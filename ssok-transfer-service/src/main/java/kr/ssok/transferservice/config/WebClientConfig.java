package kr.ssok.transferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

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
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}