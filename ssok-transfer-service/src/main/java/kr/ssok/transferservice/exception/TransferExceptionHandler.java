package kr.ssok.transferservice.exception;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 송금 서비스 전용 예외 처리 핸들러
 * 공통 예외처리 외에 송금 서비스만의 특별한 예외 처리가 필요한 경우 사용
 */
@Slf4j
@RestControllerAdvice(basePackages = "kr.ssok.transferservice")
public class TransferExceptionHandler {

    /**
     * TransferException 처리
     */
    @ExceptionHandler(TransferException.class)
    public ResponseEntity<BaseResponse<?>> handleTransferException(TransferException e) {
        ResponseStatus status = e.getStatus();
        HttpStatus httpStatus = status.getHttpStatus();

        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(httpStatus)
                .body(new BaseResponse<>(status));
    }

    /**
     * JPA 예외 처리 (예: 쿼리 실패, 제약 조건 위반 등)
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseResponse<?>> handleDataAccessException(DataAccessException e) {

        log.error("[ERROR] 데이터베이스 접근 중 예외 발생", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(
                        false,
                        500,
                        "데이터베이스 오류가 발생했습니다.",
                        null
                ));
    }
}
