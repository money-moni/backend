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
    INVALID_SIGNUP_REQUEST_VALUE(false, 4002, "유효하지 않은 회원가입 양식입니다."),
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
    PIN_CHANGE_AUTH_REQUIRED(false, 4017, "PIN 번호 변경을 위한 인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    PHONE_NUMBER_MISMATCH(false, 4018, "요청된 전화번호가 사용자 정보와 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 뱅크 서버 관련 오류
    USER_ALREADY_EXISTS(false, 5010, "이미 존재하는 사용자입니다."),
    USER_NOT_FOUND(false, 5011, "사용자를 찾을 수 없습니다."),
    BANK_SERVER_ERROR(false, 5012, "뱅크 서버와 통신 중 오류가 발생했습니다."),
    ACCOUNT_CREATION_FAILED(false, 5013, "계좌 생성에 실패했습니다."),
    TERMS_NOT_FOUND(false, 5014, "약관 정보를 찾을 수 없습니다."),
    
    // 프로필 이미지 관련 오류
    PROFILE_IMAGE_NOT_FOUND(false, 5020, "프로필 이미지를 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(false, 5021, "파일 업로드 중 오류가 발생했습니다."),
    FILE_DELETE_ERROR(false, 5022, "파일 삭제 중 오류가 발생했습니다."),
    FILE_EMPTY(false, 4020, "파일이 비어있습니다."),
    INVALID_FILE_TYPE(false, 4021, "지원하지 않는 파일 형식입니다. 이미지 파일만 업로드 가능합니다."),
    FILE_SIZE_EXCEEDED(false, 4022, "파일 크기가 5MB를 초과합니다."),

    // gRPC 내부 통신 관련 오류
    INTERNAL_SERVER_ERROR(false, 4050, "내부 서버 에러가 발생했습니다.");

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
