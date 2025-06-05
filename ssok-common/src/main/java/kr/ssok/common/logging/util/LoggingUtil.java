package kr.ssok.common.logging.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 로깅 관련 유틸리티 메서드를 제공하는 클래스
 */
public class LoggingUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // MDC 키 상수
    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_URI = "requestUri";
    public static final String HTTP_METHOD = "httpMethod";

    // 민감한 정보 마스킹 패턴
    private static final Pattern PIN_PATTERN = Pattern.compile("\"pinCode\"\\s*:\\s*\\d+");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"[^\"]*\"");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"(access|refresh)?[Tt]oken\"\\s*:\\s*\"[^\"]*\"");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\"phone\"\\s*:\\s*\"[^\"]*\"");
    
    /**
     * 객체를 JSON 문자열로 변환
     * 
     * @param obj 변환할 객체
     * @return JSON 문자열
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
    
    /**
     * 민감한 정보를 마스킹한 JSON 문자열을 반환
     * 
     * @param obj 변환할 객체
     * @return 마스킹된 JSON 문자열
     */
    public static String toMaskedJsonString(Object obj) {
        String jsonString = toJsonString(obj);
        return maskSensitiveData(jsonString);
    }
    
    /**
     * 문자열에서 민감한 정보를 마스킹
     * 
     * @param input 원본 문자열
     * @return 마스킹된 문자열
     */
    public static String maskSensitiveData(String input) {
        if (input == null) {
            return null;
        }
        
        String masked = input;
        masked = PIN_PATTERN.matcher(masked).replaceAll("\"pinCode\":\"****\"");
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("\"password\":\"****\"");
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("\"$1Token\":\"****\"");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("\"phone\":\"****\"");
        
        return masked;
    }
    
    /**
     * 메서드 시그니처를 문자열로 변환
     * 
     * @param method 메서드 객체
     * @return 메서드 시그니처 문자열
     */
    public static String getMethodSignature(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
    
    /**
     * 메서드 시그니처를 문자열로 변환
     * 
     * @param methodName 메서드 이름
     * @param targetClass 대상 클래스
     * @return 메서드 시그니처 문자열
     */
    public static String getMethodSignature(String methodName, Class<?> targetClass) {
        return targetClass.getSimpleName() + "." + methodName;
    }
    
    /**
     * 메서드 파라미터를 로깅용 문자열로 변환
     * 
     * @param method 메서드 객체
     * @param args 메서드 인자
     * @param maskSensitive 민감한 정보 마스킹 여부
     * @return 파라미터 문자열
     */
    public static String formatMethodParameters(Method method, Object[] args, boolean maskSensitive) {
        if (args == null || args.length == 0) {
            return "()";
        }
        
        String[] paramNames = Arrays.stream(method.getParameters())
                .map(param -> param.getName())
                .toArray(String[]::new);
        
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            paramMap.put(paramNames[i], args[i]);
        }
        
        return maskSensitive ? toMaskedJsonString(paramMap) : toJsonString(paramMap);
    }
    
    /**
     * MDC에 Trace ID를 설정
     * 
     * @param traceId 설정할 Trace ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID, traceId);
        }
    }
    
    /**
     * MDC에서 Trace ID를 가져옴
     * 
     * @return 현재 Trace ID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }
    
    /**
     * MDC에 사용자 ID를 설정
     * 
     * @param userId 설정할 사용자 ID
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            MDC.put(USER_ID, userId);
        }
    }
    
    /**
     * MDC에서 사용자 ID를 가져옴
     * 
     * @return 현재 사용자 ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }
    
    /**
     * MDC를 초기화
     */
    public static void clearMDC() {
        MDC.clear();
    }
    
    /**
     * 실행 시간을 사람이 읽기 쉬운 형태로 포맷
     * 
     * @param durationMs 실행 시간(밀리초)
     * @return 포맷된 시간 문자열
     */
    public static String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
}
