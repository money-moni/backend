package kr.ssok.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드에 대한 자동 로깅을 제공하는 어노테이션
 * 요청/응답 정보, 실행 시간, 파라미터 등을 자동으로 로깅
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLogging {
    
    /**
     * 로그에 포함할 추가 설명
     */
    String value() default "";
    
    /**
     * 요청 파라미터 로깅 여부
     */
    boolean logParameters() default true;
    
    /**
     * 응답 결과 로깅 여부
     */
    boolean logResult() default true;
    
    /**
     * 실행 시간 로깅 여부
     */
    boolean logExecutionTime() default true;
    
    /**
     * 민감한 정보 마스킹 여부
     */
    boolean maskSensitiveData() default true;
}
