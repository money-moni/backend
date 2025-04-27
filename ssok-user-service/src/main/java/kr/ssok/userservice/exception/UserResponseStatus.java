package kr.ssok.userservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserResponseStatus implements ResponseStatus {
    
    SUCCESS(true,2000, "요청에 성공하였습니다."),
    REGISTER_USER_SUCCESS(true, 2000, "회원가입에 성공하였습니다."),
    
    // 회원 관련 오류
    INVALID_PIN_CODE(false, 4000, "유효하지 않은 PIN 번호입니다."),

    
    // 뱅크 서버 관련 오류
    USER_ALREADY_EXISTS(false, 5000, "이미 존재하는 사용자입니다."),
    USER_NOT_FOUND(false, 5000, "사용자를 찾을 수 없습니다."),
    BANK_SERVER_ERROR(false, 5000, "뱅크 서버와 통신 중 오류가 발생했습니다."),
    ACCOUNT_CREATION_FAILED(false, 5000, "계좌 생성에 실패했습니다.");

    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    UserResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    UserResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
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
