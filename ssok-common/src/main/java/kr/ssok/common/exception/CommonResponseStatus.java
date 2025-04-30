package kr.ssok.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통으로 사용하는 응답 상태 코드
 * 각 도메인에 공통으로 사용되는 기본적인 응답 코드만 정의
 */
@Getter
public enum CommonResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000)
     */
    SUCCESS(true, 2000, "요청에 성공하였습니다."),

    /**
     * 2. 클라이언트 에러(4000)
     */
    BAD_REQUEST(false, 4000, "잘못된 요청입니다."),
    INVALID_PARAMETER(false, 4001, "유효하지 않은 파라미터입니다."),
    RESOURCE_NOT_FOUND(false, 4002, "요청한 리소스를 찾을 수 없습니다."),

    /**
     * 3. 서버 에러(5000)
     */
    INTERNAL_SERVER_ERROR(false, 5000, "서버 내부 에러가 발생했습니다."),
    UNEXPECTED_ERROR(false, 5001, "예상치 못한 에러가 발생했습니다."),
    FAIL_TO_ENCODING(false, 5002, "요청 인코딩에 실패했습니다."),
    FAIL_TO_JSON(false, 5003, "JSON 파싱 에러가 발생했습니다.");

    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    CommonResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    CommonResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
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
