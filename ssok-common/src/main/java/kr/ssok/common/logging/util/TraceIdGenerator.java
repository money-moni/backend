package kr.ssok.common.logging.util;

import java.util.UUID;

/**
 * 요청 추적을 위한 Trace ID 생성 유틸리티
 */
public class TraceIdGenerator {
    
    private static final String TRACE_ID_PREFIX = "SSOK-";
    
    /**
     * 새로운 Trace ID를 생성
     * 
     * @return SSOK- 접두사가 붙은 UUID 기반 Trace ID
     */
    public static String generate() {
        return TRACE_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    /**
     * 짧은 형태의 Trace ID를 생성
     * 
     * @return 8자리 짧은 Trace ID
     */
    public static String generateShort() {
        return TRACE_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    /**
     * Trace ID가 유효한 형식인지 확인
     * 
     * @param traceId 확인할 Trace ID
     * @return 유효한 형식이면 true
     */
    public static boolean isValid(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return false;
        }
        
        return traceId.startsWith(TRACE_ID_PREFIX) && 
               traceId.length() >= TRACE_ID_PREFIX.length() + 8;
    }
}
