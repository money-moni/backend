package kr.ssok.userservice.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Status code : {}, methodKey: {}", response.status(), methodKey);
        
        if (response.status() >= 400 && response.status() <= 499) {
            log.error("Feign call 중 client 에러: {}", response.reason());
            return new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        } else if (response.status() >= 500 && response.status() <= 599) {
            log.error("Feign call 중 server 에러: {}", response.reason());
            return new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        }
        
        return new UserException(UserResponseStatus.BANK_SERVER_ERROR);
    }
}