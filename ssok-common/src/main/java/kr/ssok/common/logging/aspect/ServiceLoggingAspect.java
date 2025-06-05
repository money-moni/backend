package kr.ssok.common.logging.aspect;

import kr.ssok.common.logging.annotation.ServiceLogging;
import kr.ssok.common.logging.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Service 로깅을 처리하는 AOP Aspect
 * @ServiceLogging 어노테이션이 적용된 메서드의 실행을 자동으로 로깅
 */
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {
    
    /**
     * @ServiceLogging 어노테이션이 적용된 메서드에 대한 Around Advice
     */
    @Around("@annotation(serviceLogging)")
    public Object logService(ProceedingJoinPoint joinPoint, ServiceLogging serviceLogging) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodSignature = LoggingUtil.getMethodSignature(method);
        String traceId = LoggingUtil.getTraceId();
        
        try {
            // 서비스 시작 로깅
            if (serviceLogging.logParameters()) {
                String parameters = LoggingUtil.formatMethodParameters(
                    method, 
                    joinPoint.getArgs(), 
                    serviceLogging.maskSensitiveData()
                );
                log.info("[{}][SERVICE-START] {} - Parameters: {}", 
                        traceId, methodSignature, parameters);
            } else {
                log.info("[{}][SERVICE-START] {}", traceId, methodSignature);
            }
            
            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            
            // 성공 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (serviceLogging.logResult() && result != null) {
                String resultString = serviceLogging.maskSensitiveData() ? 
                    LoggingUtil.toMaskedJsonString(result) : LoggingUtil.toJsonString(result);
                log.info("[{}][SERVICE-SUCCESS] {} - Result: {}, Duration: {}", 
                        traceId, methodSignature, resultString, LoggingUtil.formatDuration(executionTime));
            } else if (serviceLogging.logExecutionTime()) {
                log.info("[{}][SERVICE-SUCCESS] {} - Duration: {}", 
                        traceId, methodSignature, LoggingUtil.formatDuration(executionTime));
            } else {
                log.info("[{}][SERVICE-SUCCESS] {}", traceId, methodSignature);
            }
            
            return result;
            
        } catch (Exception e) {
            // 오류 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (serviceLogging.logException()) {
                log.error("[{}][SERVICE-ERROR] {} - Exception: {}, Duration: {}", 
                         traceId, methodSignature, e.getMessage(), LoggingUtil.formatDuration(executionTime), e);
            } else {
                log.error("[{}][SERVICE-ERROR] {} - Exception: {}, Duration: {}", 
                         traceId, methodSignature, e.getMessage(), LoggingUtil.formatDuration(executionTime));
            }
            throw e;
        }
    }
    
    /**
     * 클래스 레벨의 @ServiceLogging 어노테이션 처리
     */
    @Around("@within(serviceLogging) && execution(public * *(..))")
    public Object logServiceClass(ProceedingJoinPoint joinPoint, ServiceLogging serviceLogging) throws Throwable {
        // 메서드 레벨 어노테이션이 없는 경우에만 클래스 레벨 설정 적용
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(ServiceLogging.class)) {
            return joinPoint.proceed(); // 메서드 레벨 어노테이션이 우선
        }
        
        return logService(joinPoint, serviceLogging);
    }
}
