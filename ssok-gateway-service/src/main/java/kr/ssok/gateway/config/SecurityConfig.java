package kr.ssok.gateway.config;

import kr.ssok.gateway.security.filter.JwtAuthenticationFilter;
import kr.ssok.gateway.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 보안 관련 설정 클래스
 * Spring Security 필터 체인 및 보안 정책을 설정합니다.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final List<String> WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/users/signup",
            "/api/users/phone",
            "/api/users/phone/verify",
            "/actuator/prometheus"
    );

    /**
     * Spring Security 웹 필터 체인 구성
     * JWT 인증 필터 추가 및 보안 정책 설정
     * 
     * @param http ServerHttpSecurity 객체
     * @return 구성된 SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> 
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(WHITELIST.toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * CORS 설정
     * 크로스 오리진 리소스 공유 정책 구성
     * 
     * @return 구성된 CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
