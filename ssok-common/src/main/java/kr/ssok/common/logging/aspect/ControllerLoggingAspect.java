package kr.ssok.common.logging.aspect;

import kr.ssok.common.logging.annotation.ControllerLogging;
import kr.ssok.common.logging.util.LoggingUtil;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Controller 로깅을 처리하는 AOP Aspect
 * @ControllerLogging 어노테이션이 적용된 메서드의 요청/응답을 자동으로 로깅
 */
@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {
    
    /**
     * @ControllerLogging 어노테이션이 적용된 메서드에 대한 Around Advice
     */
    @Around("@annotation(controllerLogging)")
    public Object logController(ProceedingJoinPoint joinPoint, ControllerLogging controllerLogging) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodSignature = LoggingUtil.getMethodSignature(joinPoint.getSignature().getName(), 
                                                               joinPoint.getTarget().getClass());
        String traceId = LoggingUtil.getTraceId();
        
        try {
            // 요청 시작 로깅
            if (controllerLogging.logParameters()) {
                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                String parameters = LoggingUtil.formatMethodParameters(
                        method,
                        joinPoint.getArgs(),
                        controllerLogging.maskSensitiveData()
                );
                log.info("[{}][CONTROLLER-START] {} - Parameters: {}", 
                        traceId, methodSignature, parameters);
            } else {
                log.info("[{}][CONTROLLER-START] {}", traceId, methodSignature);
            }
            
            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            
            // 성공 응답 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (controllerLogging.logResult() && result != null) {
                String resultString = controllerLogging.maskSensitiveData() ? 
                    LoggingUtil.toMaskedJsonString(result) : LoggingUtil.toJsonString(result);
                log.info("[{}][CONTROLLER-SUCCESS] {} - Result: {}, Duration: {}", 
                        traceId, methodSignature, resultString, LoggingUtil.formatDuration(executionTime));
            } else if (controllerLogging.logExecutionTime()) {
                log.info("[{}][CONTROLLER-SUCCESS] {} - Duration: {}", 
                        traceId, methodSignature, LoggingUtil.formatDuration(executionTime));
            } else {
                log.info("[{}][CONTROLLER-SUCCESS] {}", traceId, methodSignature);
            }
            
            return result;
            
        } catch (Exception e) {
            // 오류 응답 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[{}][CONTROLLER-ERROR] {} - Exception: {}, Duration: {}", 
                     traceId, methodSignature, e.getMessage(), LoggingUtil.formatDuration(executionTime), e);
            throw e;
        }
    }
    
    /**
     * 클래스 레벨의 @ControllerLogging 어노테이션 처리
     */
    @Around("@within(controllerLogging) && execution(public * *(..))")
    public Object logControllerClass(ProceedingJoinPoint joinPoint, ControllerLogging controllerLogging) throws Throwable {
        // 메서드 레벨 어노테이션이 없는 경우에만 클래스 레벨 설정 적용
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(ControllerLogging.class)) {
            return joinPoint.proceed(); // 메서드 레벨 어노테이션이 우선
        }
        
        return logController(joinPoint, controllerLogging);
    }
}
