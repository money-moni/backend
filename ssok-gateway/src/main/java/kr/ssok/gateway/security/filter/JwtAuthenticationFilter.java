package kr.ssok.gateway.security.filter;

import kr.ssok.gateway.security.jwt.JwtVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT 인증 필터
 * 모든 요청에 대해 JWT 토큰을 검증하고 인증된 사용자 정보를 요청에 추가합니다.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtVerifier jwtVerifier;
    private final RedisTemplate<String, String> redisTemplate;
    private final List<String> whiteList;
    
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist:token:";

    public JwtAuthenticationFilter(
            JwtVerifier jwtVerifier,
            RedisTemplate<String, String> redisTemplate,
            @Value("${auth.whitelist}") List<String> whiteList) {
        this.jwtVerifier = jwtVerifier;
        this.redisTemplate = redisTemplate;
        this.whiteList = whiteList;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
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
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 필터 우선순위 설정 (낮을수록 먼저 실행)
     */
    @Override
    public int getOrder() {
        return -100; // Spring Cloud Gateway 필터 체인에서 높은 우선순위로 실행
    }
    
    /**
     * 인증이 필요없는 경로인지 확인
     */
    private boolean isWhiteListPath(String path) {
        return whiteList.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 인증 오류 처리
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.error("Authentication error: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}
