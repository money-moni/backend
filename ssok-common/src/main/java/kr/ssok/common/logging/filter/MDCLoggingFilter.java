package kr.ssok.common.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ssok.common.logging.util.LoggingUtil;
import kr.ssok.common.logging.util.TraceIdGenerator;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.ServletException;


import java.io.IOException;

/**
 * MDC(Mapped Diagnostic Context)를 설정하는 필터
 * 로그에 일관된 컨텍스트 정보를 포함시키기 위해 사용
 */
public class MDCLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String traceId = resolveTraceId(request);
            LoggingUtil.setTraceId(traceId);
            
            // 사용자 ID 설정 (Gateway에서 설정한 헤더에서 가져옴)
            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                LoggingUtil.setUserId(userId);
            }
            
            // 요청 정보 설정
            MDC.put(LoggingUtil.REQUEST_URI, request.getRequestURI());
            MDC.put(LoggingUtil.HTTP_METHOD, request.getMethod());
            
            // 다음 필터 실행
            filterChain.doFilter(request, response);
            
        } finally {
            // MDC 정리
            LoggingUtil.clearMDC();
        }
    }
    /**
     * TraceId 우선순위 처리
     */
    private String resolveTraceId(HttpServletRequest request) {
        // 1순위: Gateway에서 전달된 X-Trace-ID 헤더
        String traceId = request.getHeader("X-Trace-ID");
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }

        // 2순위: 신규 생성 (Gateway 우회 직접 호출)
        return TraceIdGenerator.generate();
    }
}
