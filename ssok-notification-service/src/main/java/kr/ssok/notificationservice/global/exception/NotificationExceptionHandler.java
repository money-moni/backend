package kr.ssok.notificationservice.global.exception;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 알림 서비스 전용 예외 처리 핸들러
 * 공통 예외처리 외에 송금 서비스만의 특별한 예외 처리가 필요한 경우 사용
 */
@RestControllerAdvice(basePackages = "kr.ssok.notificationservice")
public class NotificationExceptionHandler {

    /**
     * NotificationException 처리
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<BaseResponse<?>> handleTransferException(NotificationException e) {
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
