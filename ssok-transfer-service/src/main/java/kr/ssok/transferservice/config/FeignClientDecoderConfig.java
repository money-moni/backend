package kr.ssok.transferservice.config;

import feign.codec.ErrorDecoder;
import kr.ssok.transferservice.exception.feign.FeignClientGlobalErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 예외처리를 수행하는 Config
 */
@Configuration
public class FeignClientDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignClientGlobalErrorDecoder();
    }
}