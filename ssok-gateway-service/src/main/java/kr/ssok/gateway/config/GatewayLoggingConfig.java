package kr.ssok.gateway.config;

import kr.ssok.gateway.security.filter.ReactiveLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 로깅 설정
 */
@Configuration
public class GatewayLoggingConfig {

    @Bean
    public ReactiveLoggingFilter reactiveLoggingFilter() {
        return new ReactiveLoggingFilter();
    }
}