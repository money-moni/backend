package kr.ssok.accountservice.config;

import feign.codec.ErrorDecoder;
import kr.ssok.accountservice.exception.feign.FeignClientGlobalErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 클라이언트용 전역 예외 디코더 설정 클래스.
 *
 * <p>이 설정 클래스는 Spring 컨텍스트에 {@link ErrorDecoder} 빈을 등록하여
 * OpenFeign 클라이언트에서 자동으로 이를 사용하게 합니다.</p>
 */
@Configuration
public class FeignClientDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignClientGlobalErrorDecoder();
    }
}
