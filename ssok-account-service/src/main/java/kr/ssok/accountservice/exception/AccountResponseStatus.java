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
    ACCOUNT_CREATE_SUCCESS(true, 2201, "연동 계좌 등록을 완료했습니다."),
    ACCOUNT_GET_SUCCESS(true, 2200, "연동 계좌 조회를 완료했습니다."),
    ACCOUNT_DELETE_SUCCESS(true, 2200, "연동 계좌 삭제를 완료했습니다."),
    ACCOUNT_ALIAS_UPDATE_SUCCESS(true, 2200, "연동 계좌 별명 수정을 완료했습니다."),
    ACCOUNT_PRIMARY_UPDATE_SUCCESS(true, 2200, "주계좌 변경을 완료했습니다."),


    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    ACCOUNT_NOT_FOUND(false, 4204, "요청하신 계좌가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS(false, 4209, "이미 등록된 계좌입니다.", HttpStatus.CONFLICT),
    ACCOUNT_ALREADY_PRIMARY(false, 4209, "이미 해당 계좌는 주계좌로 설정되어 있습니다.", HttpStatus.CONFLICT),
    ACCOUNT_PRIMARY_CANNOT_DELETE(false, 4200, "주계좌는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_CREATION_FORBIDDEN(false, 4203, "본인 명의의 계좌만 연동할 수 있습니다.", HttpStatus.FORBIDDEN),
    INVALID_BANK_CODE(false, 4200, "유효하지 않은 은행 코드입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_TYPE(false, 4200, "유효하지 않은 계좌 유형입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_ALIAS(false, 4200, "계좌 별칭이 올바른 형식이 아닙니다.", HttpStatus.BAD_REQUEST),

    OPENBANKING_ACCOUNT_LIST_FAILED(false, 9001, "오픈뱅킹 계좌 목록 조회에 실패했습니다."),
    OPENBANKING_BALANCE_LOOKUP_FAILED(false, 9002, "오픈뱅킹 계좌 잔액 조회에 실패했습니다."),
    OPENBANKING_OWNER_LOOKUP_FAILED(false, 9003, "오픈뱅킹 실명 조회에 실패했습니다."),

    USER_INFO_NOT_FOUND(false, 9001, "사용자 정보를 찾을 수 없습니다.");

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
