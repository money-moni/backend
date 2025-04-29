package kr.ssok.userservice.exception;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.ResponseStatus;
import kr.ssok.userservice.dto.response.ExtendedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 사용자 서비스 전용 예외 처리 핸들러
 * 공통 예외처리 외에 사용자 서비스만의 특별한 예외 처리가 필요한 경우 사용
 */
@RestControllerAdvice(basePackages = "kr.ssok.userservice")
public class UserExceptionHandler {

    /**
     * UserException 처리
     * 사용자 인증 관련 예외 발생 시 추가 정보를 포함한 응답 생성
     * 
     * @param e 발생한 UserException
     * @return 확장된 응답을 포함한 ResponseEntity
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<BaseResponse<?>> handleUserException(UserException e) {
        ResponseStatus status = e.getStatus();
        HttpStatus httpStatus = status.getHttpStatus();

        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        
        // 추가 정보가 있는 경우 ExtendedResponse 사용
        if (!e.getAdditionalInfo().isEmpty()) {
            ExtendedResponse<?> response = new ExtendedResponse<>(status);
            response.setAdditionalInfo(e.getAdditionalInfo());
            
            return ResponseEntity
                    .status(httpStatus)
                    .body(response);
        }

        // 기본 응답
        return ResponseEntity
                .status(httpStatus)
                .body(new BaseResponse<>(status));
    }
}
