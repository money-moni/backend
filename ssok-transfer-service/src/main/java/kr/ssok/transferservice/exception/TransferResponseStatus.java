package kr.ssok.transferservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 송금 서비스 관련 응답 상태 코드
 */
@Getter
public enum TransferResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000~2999)
     */
    TRANSFER_SUCCESS(true, 2000, "송금이 성공적으로 완료되었습니다."),
    TRANSFER_HISTORY_SUCCESS(true, 2000, "송금 내역 조회를 완료했습니다."),
    TRANSFER_COUNTERPART_SUCCESS(true, 2000, "최근 송금 내역 조회가 완료되었습니다."),

    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    TRANSFER_FAILED(false, 4000, "송금에 실패했습니다."),
    ACCOUNT_LOOKUP_FAILED(false, 4001, "계좌 조회에 실패했습니다."),
    REMITTANCE_FAILED(false, 4002, "송금 요청에 실패했습니다."),
    INVALID_TRANSFER_AMOUNT(false, 4003, "송금 금액은 0보다 커야 합니다."),
    INVALID_ACCOUNT_ID(false, 4004, "계좌 ID는 필수입니다."),
    INVALID_USER_ID(false, 4005, "USER ID는 필수입니다.");

    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    TransferResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    TransferResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
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
