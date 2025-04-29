package kr.ssok.userservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserResponseStatus implements ResponseStatus {
    
    SUCCESS(true,2000, "요청에 성공하였습니다."),
    REGISTER_USER_SUCCESS(true, 2000, "회원가입에 성공하였습니다."),
    LOGIN_SUCCESS(true, 2001, "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(true, 2002, "로그아웃에 성공하였습니다."),
    TOKEN_REFRESH_SUCCESS(true, 2003, "토큰 갱신에 성공하였습니다."),
    
    // 회원 관련 오류
    INVALID_PIN_CODE(false, 4000, "유효하지 않은 PIN 번호입니다."),
    CODE_VERIFICATION_FAIL(false, 4001, "휴대폰 인증번호가 일치하지 않아, 인증에 실패했습니다."),
    
    // 인증 관련 오류
    INVALID_TOKEN(false, 4010, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(false, 4011, "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(false, 4012, "유효하지 않은 리프레시 토큰입니다."),
    BLACKLISTED_TOKEN(false, 4013, "이미 로그아웃된 토큰입니다."),
    LOGIN_FAILED(false, 4014, "로그인에 실패했습니다."),
    TOO_MANY_LOGIN_ATTEMPTS(false, 4015, "로그인 시도 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    ACCOUNT_LOCKED(false, 4016, "계정이 잠금 상태입니다."),
    
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
