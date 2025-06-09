package kr.ssok.common.logging.filter;

import kr.ssok.common.logging.util.LoggingUtil;
import kr.ssok.common.logging.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP 요청/응답을 로깅하는 필터
 * 모든 HTTP 요청에 대해 Trace ID를 생성하고 요청/응답 정보를 로깅
 */
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    // 로깅에서 제외할 경로들
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/actuator", "/health", "/metrics", "/prometheus", "/favicon.ico",
            "/swagger", "/api-docs", "/webjars"
    );

    // 로깅에서 제외할 Content-Type들
    private static final List<String> EXCLUDED_CONTENT_TYPES = Arrays.asList(
            "image/", "video/", "audio/", "application/octet-stream"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 제외 경로 확인
        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // MDC에서 이미 설정된 TraceId 사용
        String traceId = LoggingUtil.getTraceId();

        // TraceId가 없으면 새로 생성
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdGenerator.generate();
            LoggingUtil.setTraceId(traceId);
        }

        // 요청/응답 래퍼 생성 (내용 캐싱을 위해)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // 요청 정보 로깅
            logRequest(wrappedRequest, traceId);

            // X-User-ID 헤더에서 사용자 ID 추출 및 MDC 설정 (MDCLoggingFilter에서 이미 설정됐지만 안전장치)
            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                LoggingUtil.setUserId(userId);
                MDC.put(LoggingUtil.USER_ID, userId);
            }

            // 요청 URI와 HTTP 메서드 MDC 설정 (MDCLoggingFilter에서 이미 설정됐지만 안전장치)
            MDC.put(LoggingUtil.REQUEST_URI, request.getRequestURI());
            MDC.put(LoggingUtil.HTTP_METHOD, request.getMethod());

            // 실제 요청 처리
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // 응답 정보 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            logResponse(wrappedResponse, traceId, executionTime);

            // 응답 내용을 실제 응답으로 복사
            wrappedResponse.copyBodyToResponse();

//            try {
//                // 응답 정보 로깅
//                long executionTime = System.currentTimeMillis() - startTime;
//                logResponse(wrappedResponse, traceId, executionTime);
//
//                // 응답 내용을 실제 응답으로 복사
//                wrappedResponse.copyBodyToResponse();
//
//            } finally {
//                // MDC는 MDCLoggingFilter에서 정리하므로 여기서는 정리하지 않음
//                // LoggingUtil.clearMDC(); // 제거
//            }
        }
    }

    /**
     * 로깅을 건너뛸지 판단
     */
    private boolean shouldSkipLogging(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }

    /**
     * 요청 정보를 로깅
     */
    private void logRequest(ContentCachingRequestWrapper request, String traceId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        // 기본 요청 정보 로깅
        if (queryString != null) {
            log.info("[{}][HTTP-REQUEST] {} {}", traceId, method, uri + "?" + queryString);
        } else {
            log.info("[{}][HTTP-REQUEST] {} {}", traceId, method, uri);
        }

        // 요청 헤더 로깅 (민감한 헤더 제외)
        logRequestHeaders(request, traceId);

        // 요청 바디 로깅 (POST, PUT, PATCH인 경우)
        if (shouldLogRequestBody(request)) {
            logRequestBody(request, traceId);
        }
    }

    /**
     * 요청 헤더를 로깅
     */
    private void logRequestHeaders(HttpServletRequest request, String traceId) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!isSensitiveHeader(headerName)) {
                String headerValue = request.getHeader(headerName);
                headers.append(headerName).append(": ").append(headerValue).append(", ");
            }
        });

        if (headers.length() > 0) {
            headers.setLength(headers.length() - 2);
            log.debug("[{}][HTTP-REQUEST-HEADERS] {}", traceId, headers.toString());
        }
    }

    /**
     * 요청 바디를 로깅
     */
    private void logRequestBody(ContentCachingRequestWrapper request, String traceId) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String body = new String(content);
                String maskedBody = LoggingUtil.maskSensitiveData(body);
                log.info("[{}][HTTP-REQUEST-BODY] {}", traceId, maskedBody);
            }
        }
    }

    /**
     * 응답 정보를 로깅
     */
    private void logResponse(ContentCachingResponseWrapper response, String traceId, long executionTime) {
        int status = response.getStatus();
        String formattedDuration = LoggingUtil.formatDuration(executionTime);

        log.info("[{}][HTTP-RESPONSE] Status: {}, Duration: {}", traceId, status, formattedDuration);

        // 응답 바디 로깅 (오류 응답이거나 디버그 레벨인 경우)
        if (status >= 400 || log.isDebugEnabled()) {
            logResponseBody(response, traceId);
        }

        // 성능 경고 (5초 초과 시)
        if (executionTime > 5000) {
            log.warn("[{}][HTTP-SLOW-REQUEST] Request took {} to complete", traceId, formattedDuration);
        }
    }

    /**
     * 응답 바디를 로깅
     */
    private void logResponseBody(ContentCachingResponseWrapper response, String traceId) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String body = new String(content);
                String maskedBody = LoggingUtil.maskSensitiveData(body);
                log.debug("[{}][HTTP-RESPONSE-BODY] {}", traceId, maskedBody);
            }
        }
    }

    /**
     * 요청 바디를 로깅할지 판단.
     */
    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();

        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
            return false;
        }

        if (contentType == null) {
            return false;
        }

        return EXCLUDED_CONTENT_TYPES.stream().noneMatch(contentType::startsWith);
    }

    /**
     * 민감한 헤더인지 확인.
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization") ||
                lowerCaseName.contains("cookie") ||
                lowerCaseName.contains("token");
    }
}
