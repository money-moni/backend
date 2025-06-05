package kr.ssok.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 성능 측정 및 로깅을 제공하는 어노테이션
 * 메서드 실행 시간을 측정하고 성능 임계값을 초과할 경우 경고 로그 남김
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLogging {
    
    /**
     * 로그에 포함할 추가 설명
     */
    String value() default "";
    
    /**
     * 성능 경고 임계값 (밀리초)
     * 실행 시간이 이 값을 초과하면 경고 로그를 남김
     */
    long warningThresholdMs() default 1000;
    
    /**
     * 성능 오류 임계값 (밀리초)
     * 실행 시간이 이 값을 초과하면 오류 로그를 남김
     */
    long errorThresholdMs() default 5000;
    
    /**
     * 실행 시간을 항상 로깅할지 여부
     * false인 경우 임계값을 초과한 경우에만 로깅
     */
    boolean alwaysLog() default false;
}
