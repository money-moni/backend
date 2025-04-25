package kr.ssok.userservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 사용자 서비스 관련 응답 상태 코드
 */
@Getter
public enum UserResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000~2999)
     */
    // 1-1. 회원가입 / 로그인 / 로그아웃 / 회원탈퇴
    VERIFICATION_REGISTER_ACCOUNT_SUCCESS(true, 2100, "회원가입 요구 정보 검증에 성공했습니다."),
    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    // 2-1. 회원가입 / 로그인 / 로그아웃 / 회원탈퇴
    EMAIL_ALREADY_EXISTS(false, 4100, "이미 가입된 이메일입니다."),

    // 2-2. 인가 / 인증
    INVALID_BEARER_GRANT_TYPE(false, 4200, "Bearer 타입이 아닙니다.", HttpStatus.UNAUTHORIZED);

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
