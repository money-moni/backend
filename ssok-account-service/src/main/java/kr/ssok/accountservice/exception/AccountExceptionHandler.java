package kr.ssok.accountservice.exception;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 계좌 서비스 전용 예외 처리 핸들러
 * 공통 예외처리 외에 계좌 서비스만의 특별한 예외 처리가 필요한 경우 사용
 */
@RestControllerAdvice(basePackages = "kr.ssok.accountservice")
public class AccountExceptionHandler {

    /**
     * AccountException 처리
     */
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<BaseResponse<?>> handleAccountException(AccountException e) {
        ResponseStatus status = e.getStatus();
        HttpStatus httpStatus = status.getHttpStatus();

        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(httpStatus)
                .body(new BaseResponse<>(status));
    }
}
