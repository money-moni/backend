package kr.ssok.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 오픈뱅킹 외부 서버와의 통신을 위한 {@link WebClient} Bean을 등록하는 설정 클래스
 */
@Configuration
public class WebClientConfig {
    @Value("${external.openbanking-service.url}")
    private String baseUrl;

    @Bean
    public WebClient openBankingWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
