package kr.ssok.gateway.security.filter;

import kr.ssok.gateway.security.jwt.JwtVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰을 검증하고 인증된 사용자 정보를 SecurityContext에 설정합니다.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtVerifier jwtVerifier;
    private final RedisTemplate<String, String> redisTemplate;
    private final List<String> whiteList;
    
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist:token:";

    public JwtAuthenticationFilter(
            JwtVerifier jwtVerifier,
            RedisTemplate<String, String> redisTemplate) {
        this.jwtVerifier = jwtVerifier;
        this.redisTemplate = redisTemplate;
        this.whiteList = List.of(
                "/api/auth/login",
                "/api/auth/refresh",
                "/api/users/signup",
                "/api/users/phone",
                "/api/users/phone/verify",
                "/actuator/prometheus",
                "/actuator",
                "/actuator/health",
                "/actuator/info",
                "/chaos/account-service/actuator/chaosmonkey",
                "/chaos/transfer-service",
                "/chaos/user-service",
                "/chaos/notification-service",
                "/chaos/bluetooth-service"
        );
    }

    /**
     * 요청을 필터링하여 JWT 토큰 검증 및 인증 정보 설정
     * 
     * @param exchange 요청/응답 교환 객체
     * @param chain 다음 필터 체인
     * @return 필터 체인 실행 결과
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // 인증이 필요없는 요청은 토큰 검증 없이 통과
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }
        
        // 인증 헤더 확인
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Authorization header is missing or invalid", HttpStatus.UNAUTHORIZED);
        }
        
        // 토큰 추출 및 검증
        String token = jwtVerifier.resolveToken(authHeader);
        if (!jwtVerifier.validateToken(token)) {
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
        
        // 블랙리스트 확인
        String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
        Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            return onError(exchange, "Token is blacklisted", HttpStatus.UNAUTHORIZED);
        }
        
        // 토큰에서 사용자 ID 추출
        Long userId = jwtVerifier.getUserIdFromToken(token);
        if (userId == null) {
            return onError(exchange, "Could not extract user ID from token", HttpStatus.UNAUTHORIZED);
        }
        
        // 요청에 사용자 ID 추가
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-ID", userId.toString())
                .build();
        
        // Spring Security의 인증 정보 설정
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
    
    /**
     * 인증이 필요 없는 경로인지 확인
     * 
     * @param path 요청 경로
     * @return 화이트리스트 포함 여부
     */
    private boolean isWhiteListPath(String path) {
        return whiteList.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 인증 오류 처리
     * 
     * @param exchange 요청/응답 교환 객체
     * @param message 오류 메시지
     * @param status HTTP 상태 코드
     * @return 오류 응답
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("Authentication error: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}
