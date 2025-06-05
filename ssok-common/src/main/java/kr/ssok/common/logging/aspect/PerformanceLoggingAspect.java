package kr.ssok.common.logging.aspect;

import kr.ssok.common.logging.annotation.PerformanceLogging;
import kr.ssok.common.logging.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 성능 로깅을 처리하는 AOP Aspect
 * @PerformanceLogging 어노테이션이 적용된 메서드의 성능을 측정하고 로깅
 */
@Slf4j
@Aspect
@Component
public class PerformanceLoggingAspect {
    
    /**
     * @PerformanceLogging 어노테이션이 적용된 메서드에 대한 Around Advice
     */
    @Around("@annotation(performanceLogging)")
    public Object logPerformance(ProceedingJoinPoint joinPoint, PerformanceLogging performanceLogging) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodSignature = LoggingUtil.getMethodSignature(method);
        String traceId = LoggingUtil.getTraceId();
        
        try {
            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            
            // 성능 측정 및 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            String formattedDuration = LoggingUtil.formatDuration(executionTime);
            
            if (performanceLogging.alwaysLog()) {
                // 항상 로깅
                log.info("[{}][PERFORMANCE] {} - Duration: {}", traceId, methodSignature, formattedDuration);
            } else if (executionTime >= performanceLogging.errorThresholdMs()) {
                // 오류 임계값 초과
                log.error("[{}][PERFORMANCE-ERROR] {} - Duration: {} (Threshold: {}ms)", 
                         traceId, methodSignature, formattedDuration, performanceLogging.errorThresholdMs());
            } else if (executionTime >= performanceLogging.warningThresholdMs()) {
                // 경고 임계값 초과
                log.warn("[{}][PERFORMANCE-WARNING] {} - Duration: {} (Threshold: {}ms)", 
                        traceId, methodSignature, formattedDuration, performanceLogging.warningThresholdMs());
            }
            
            return result;
            
        } catch (Exception e) {
            // 예외 발생 시에도 성능 측정
            long executionTime = System.currentTimeMillis() - startTime;
            String formattedDuration = LoggingUtil.formatDuration(executionTime);
            
            log.error("[{}][PERFORMANCE-EXCEPTION] {} - Duration: {}, Exception: {}", 
                     traceId, methodSignature, formattedDuration, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 클래스 레벨의 @PerformanceLogging 어노테이션 처리
     */
    @Around("@within(performanceLogging) && execution(public * *(..))")
    public Object logPerformanceClass(ProceedingJoinPoint joinPoint, PerformanceLogging performanceLogging) throws Throwable {
        // 메서드 레벨 어노테이션이 없는 경우에만 클래스 레벨 설정 적용
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(PerformanceLogging.class)) {
            return joinPoint.proceed(); // 메서드 레벨 어노테이션이 우선
        }
        
        return logPerformance(joinPoint, performanceLogging);
    }
}
