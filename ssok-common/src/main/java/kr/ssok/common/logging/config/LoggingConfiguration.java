package kr.ssok.common.logging.config;

import feign.RequestInterceptor;
import kr.ssok.common.logging.feign.FeignLoggingInterceptor;
import kr.ssok.common.logging.filter.MDCLoggingFilter;
import kr.ssok.common.logging.filter.RequestResponseLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 로깅 관련 설정을 담당하는 Configuration 클래스
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfiguration {
    
    /**
     * MDC 로깅 필터를 등록
     * 가장 높은 우선순위로 설정하여 모든 요청에 대해 MDC를 설정
     */
    @Bean
    public FilterRegistrationBean<MDCLoggingFilter> mdcLoggingFilter() {
        FilterRegistrationBean<MDCLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MDCLoggingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // 가장 높은 우선순위
        registrationBean.setName("mdcLoggingFilter");
        return registrationBean;
    }
    
    /**
     * 요청/응답 로깅 필터를 등록
     * MDC 필터 다음 우선순위로 설정
     */
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2); // MDC 필터 다음 우선순위
        registrationBean.setName("requestResponseLoggingFilter");
        return registrationBean;
    }

    /**
     * Feign 로깅 인터셉터 등록
     */
    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    public FeignLoggingInterceptor feignLoggingInterceptor() {
        return new FeignLoggingInterceptor();
    }
}
