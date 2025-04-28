package kr.ssok.accountservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 계좌 서비스 관련 응답 상태 코드
 */
@Getter
public enum AccountResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000~2999)
     */
    ACCOUNT_CREATE_SUCCESS(true, 2000, "계좌 생성이 성공적으로 완료되었습니다."),

    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    ACCOUNT_NOT_FOUND(false, 4000, "계좌를 찾을 수 없습니다."),
    ACCOUNT_ALREADY_EXISTS(false, 4001, "이미 계좌가 존재합니다.");

    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    AccountResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    AccountResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
