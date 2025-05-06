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
    REGISTER_SUCCESS(true, 2000, "Bluetooth UUID가 정상적으로 등록되었습니다."),


    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    UUID_REQUIRED(false, 4001, "UUID는 필수입니다."),
    USER_ID_REQUIRED(false, 4002, "유저 ID가 없습니다."),

    /**
     * 3. 서버 에러 (5000~5999)
     */
    REDIS_ACCESS_FAILED(false, 5000, "Redis 처리 중 서버 오류가 발생했습니다.");

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
