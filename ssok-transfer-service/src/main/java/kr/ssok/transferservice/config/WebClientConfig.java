package kr.ssok.transferservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * WebClient를 통한 외부 OpenBanking API 호출을 설정하는 클래스
 */
@Configuration
public class WebClientConfig {

    /**
     * 오픈뱅킹 서버 URL
     */
    @Value("${external.openbanking-service.url}")
    private String baseUrl;

    /**
     * WebClient.Builder 빈 등록
     * 다른 컴포넌트에서 DI 받아 커스터마이징 없이 재사용할 수 있음
     *
     * @return WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * OpenBanking API 전용 WebClient 빈 등록
     * Builder를 통해 baseUrl만 지정한 단순한 형태
     *
     * @param builder WebClient.Builder
     * @return baseUrl이 설정된 WebClient
     */
    @Bean
    public WebClient openBankingWebClient(WebClient.Builder builder) {
        // Netty 기반 HTTP 커넥션 풀 설정 (동시 송금 요청 성능 향상)
        ConnectionProvider provider = ConnectionProvider.builder("openbanking-pool")
                .maxConnections(1000)             // 최대 동시 커넥션 수
                .pendingAcquireMaxCount(-1)       // 커넥션 대기열 무제한
                .pendingAcquireTimeout(Duration.ofSeconds(5)) // 커넥션 대기 최대 5초
                .build();

        // HTTP 클라이언트에 타임아웃 등 세부 옵션 설정
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)   // 연결 타임아웃 3초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(3))        // 읽기 타임아웃 3초
                        .addHandlerLast(new WriteTimeoutHandler(3)))      // 쓰기 타임아웃 3초
                .responseTimeout(Duration.ofSeconds(3));              // 전체 응답 타임아웃 3초

        // 설정한 HttpClient를 WebClient에 적용
        return builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}