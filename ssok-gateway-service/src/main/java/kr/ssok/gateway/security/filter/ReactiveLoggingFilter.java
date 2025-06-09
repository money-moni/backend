package kr.ssok.gateway.security.filter;

import kr.ssok.common.logging.util.LoggingUtil;
import kr.ssok.common.logging.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Arrays;
import java.util.List;

/**
 * WebFlux 환경에서 요청/응답을 로깅하는 Global Filter
 */
@Slf4j
@Component
public class ReactiveLoggingFilter implements GlobalFilter, Ordered {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/actuator", "/health", "/metrics", "/prometheus", "/favicon.ico"
    );

    private static final String TRACE_ID_KEY = "traceId";
    private static final String USER_ID_KEY = "userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 제외 경로 확인
        if (shouldSkipLogging(path)) {
            return chain.filter(exchange);
        }

        // Trace ID 생성
        String traceId = TraceIdGenerator.generate();
        long startTime = System.currentTimeMillis();

        // 요청 정보 로깅
        logRequest(request, traceId);

        // 사용자 ID 추출 (JWT 필터에서 설정한 헤더)
        String userId = request.getHeaders().getFirst("X-User-Id");

        // 요청에 Trace ID 헤더 추가
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Trace-ID", traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    // 응답 로깅
                    long executionTime = System.currentTimeMillis() - startTime;
                    logResponse(mutatedExchange.getResponse(), traceId, executionTime);
                })
                .doOnError(throwable -> {
                    // 오류 로깅
                    long executionTime = System.currentTimeMillis() - startTime;
                    logError(throwable, traceId, executionTime);
                })
                .contextWrite(Context.of(
                        TRACE_ID_KEY, traceId,
                        USER_ID_KEY, userId != null ? userId : "anonymous"
                ));
    }

    private boolean shouldSkipLogging(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private void logRequest(ServerHttpRequest request, String traceId) {
        String method = request.getMethod().name();
        String uri = request.getPath().value();
        String queryString = request.getURI().getQuery();

        if (queryString != null) {
            log.info("[{}][GATEWAY-REQUEST] {} {}?{}", traceId, method, uri, queryString);
        } else {
            log.info("[{}][GATEWAY-REQUEST] {} {}", traceId, method, uri);
        }
    }

    private void logResponse(ServerHttpResponse response, String traceId, long executionTime) {
        int status = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        String formattedDuration = LoggingUtil.formatDuration(executionTime);

        log.info("[{}][GATEWAY-RESPONSE] Status: {}, Duration: {}", traceId, status, formattedDuration);

        // 성능 경고 (3초 초과 시)
        if (executionTime > 3000) {
            log.warn("[{}][GATEWAY-SLOW-REQUEST] Request took {} to complete", traceId, formattedDuration);
        }
    }

    private void logError(Throwable throwable, String traceId, long executionTime) {
        String formattedDuration = LoggingUtil.formatDuration(executionTime);
        log.error("[{}][GATEWAY-ERROR] Exception: {}, Duration: {}",
                traceId, throwable.getMessage(), formattedDuration, throwable);
    }

    @Override
    public int getOrder() {
        return -1; // JWT 필터보다 먼저 실행
    }
}