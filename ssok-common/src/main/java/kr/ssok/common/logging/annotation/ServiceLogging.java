package kr.ssok.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service 메서드에 대한 자동 로깅을 제공하는 어노테이션
 * 메서드 시작/종료, 실행 시간, 파라미터, 반환값 등을 자동으로 로깅
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLogging {
    
    /**
     * 로그에 포함할 추가 설명
     */
    String value() default "";
    
    /**
     * 파라미터 로깅 여부
     */
    boolean logParameters() default true;
    
    /**
     * 반환값 로깅 여부
     */
    boolean logResult() default false;
    
    /**
     * 실행 시간 로깅 여부
     */
    boolean logExecutionTime() default true;
    
    /**
     * 예외 발생 시 상세 로깅 여부
     */
    boolean logException() default true;
    
    /**
     * 민감한 정보 마스킹 여부
     */
    boolean maskSensitiveData() default true;
}
