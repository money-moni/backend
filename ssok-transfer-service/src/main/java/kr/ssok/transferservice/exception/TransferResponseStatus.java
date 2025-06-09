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
    TRANSFER_SUCCESS(true, 2300, "송금이 성공적으로 완료되었습니다."),
    BLUETOOTH_TRANSFER_SUCCESS(true, 2300, "블루투스 송금이 성공적으로 완료되었습니다."),
    TRANSFER_HISTORY_SUCCESS(true, 2300, "송금 내역 조회를 완료했습니다."),
    TRANSFER_COUNTERPART_SUCCESS(true, 2300, "최근 송금한 상대 계좌 목록 조회가 완료되었습니다."),
    TRANSFER_RECENT_HISTORY_SUCCESS(true, 2300, "최근 송금 내역 조회가 완료되었습니다."),

    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    TRANSFER_FAILED(false, 4300, "송금에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOOKUP_FAILED(false, 4301, "계좌 조회에 실패했습니다.", HttpStatus.BAD_REQUEST),
    COUNTERPART_ACCOUNT_LOOKUP_FAILED(false, 4302, "상대방 계좌 조회에 실패했습니다.", HttpStatus.BAD_REQUEST),
    REMITTANCE_FAILED(false, 4303, "송금 요청에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TRANSFER_AMOUNT(false, 4304, "송금 금액은 0보다 커야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_ID(false, 4305, "계좌 ID는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_USER_ID(false, 4306, "USER ID는 필수입니다.", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER_NOT_ALLOWED(false, 4315, "출금 계좌와 입금 계좌가 동일합니다.", HttpStatus.BAD_REQUEST),

    // openbanking-feign 에러 처리
    ACCOUNT_NOT_FOUND(false, 4307, "유효하지 않은 계좌입니다.", HttpStatus.NOT_FOUND),
    DORMANT_ACCOUNT(false, 4308, "휴면 계좌입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(false, 4309, "해당 계좌는 잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    WITHDRAWAL_ERROR(false, 4310, "출금 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DEPOSIT_ERROR(false, 4311, "입금 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSFER_LIMIT_EXCEEDED(false, 4312, "해당 계좌의 출금 한도에 도달하였습니다.", HttpStatus.BAD_REQUEST),
    TRANSFER_UNKNOWN_ERROR(false, 4313, "송금 처리 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BANK_API_COMMUNICATION_FAILED(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),

    // gRPC 에러 처리
    aa(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    bb(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    cc(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    dd(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    ee(false, 4314, "은행 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    ;

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
