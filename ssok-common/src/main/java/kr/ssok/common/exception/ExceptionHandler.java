package kr.ssok.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * 모든 도메인에서 공통으로 사용할 예외 처리 로직 정의
 */
@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(value = {BaseException.class})
    protected ResponseEntity<BaseResponse<?>> handleBaseException(BaseException e) {
        ResponseStatus status = e.getStatus();
        HttpStatus httpStatus = status.getHttpStatus();

        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(httpStatus)
                .body(new BaseResponse<>(status));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(false, CommonResponseStatus.INVALID_PARAMETER.getCode(), errorMessage, null));
    }
    
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(CommonResponseStatus.INTERNAL_SERVER_ERROR));
    }
}
