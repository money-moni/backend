package kr.ssok.bluetoothservice.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 송금 서비스 관련 응답 상태 코드
 */
@Getter
public enum BluetoothResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000~2999)
     */
    REGISTER_SUCCESS(true, 2400, "Bluetooth UUID가 정상적으로 등록되었습니다."),
    MATCH_SUCCESS(true, 2400, "Bluetooth UUID에 대한 유저가 조회되었습니다."),
    BLUETOOTH_TRANSFER_SUCCESS(true, 2400, "블루투스 송금이 성공적으로 완료되었습니다."),

    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    TRANSFER_FAILED(false, 4400, "블루투스 송금에 실패했습니다.", HttpStatus.BAD_REQUEST),
    UUID_REQUIRED(false, 4401, "UUID는 필수입니다.", HttpStatus.BAD_REQUEST),
    USER_ID_REQUIRED(false, 4402, "유저 ID가 없습니다.", HttpStatus.BAD_REQUEST),
    NO_MATCH_FOUND(false, 4403, "UUID와 매칭된 유저가 없습니다.", HttpStatus.NOT_FOUND),
    NO_SCAN_UUID(false, 4404, "스캔된 UUID가 없습니다.", HttpStatus.BAD_REQUEST),
    COUNTERPART_ACCOUNT_LOOKUP_FAILED(false, 4407, "상대방 계좌 조회에 실패했습니다.", HttpStatus.BAD_REQUEST),
    USER_INFO_NOT_FOUND(false, 4408, "사용자 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),

    UNSUPPORTED_CODE(false, 4406, "[USER|ACCOUNT - BLUETOOTH] 지원하지 않는 예외처리 코드입니다."),
    GRPC_METADATA_INVALID(false, 4407, "[USER|ACCOUNT - BLUETOOTH] gRPC 메타 데이터가 유효하지 않습니다."),

    /**
     * 3. 서버 에러 (5000~5999)
     */
    REDIS_ACCESS_FAILED(false, 5400, "Redis 처리 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_SERVER_ERROR(false, 5401, "[ACCOUNT - BLUETOOTH] ACCOUNT 서버에 에러가 발생했습니다."),
    USER_SERVER_ERROR(false, 5402, "[USER - BLUETOOTH] USER 서버에 에러가 발생했습니다.");


    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    BluetoothResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    BluetoothResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
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
