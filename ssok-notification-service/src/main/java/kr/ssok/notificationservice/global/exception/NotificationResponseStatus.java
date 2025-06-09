package kr.ssok.notificationservice.global.exception;

import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 알림 서비스 관련 응답 상태 코드
 */
@Getter
public enum NotificationResponseStatus implements ResponseStatus {
    /**
     * 1. 요청에 성공한 경우(2000~2999)
     */
    TOKEN_REGISTER_SUCCESS(true, 2500, "FCM 토큰 등록에 성공했습니다."),

    /**
     * 2. 클라이언트 에러(4000~4999)
     */
    TOKEN_REGISTER_FAILED(false, 4500, "FCM 토큰 등록에 실패했습니다.", HttpStatus.BAD_REQUEST),
    TOKEN_REQUIRED(false, 4501, "FCM 토큰이 필요합니다.", HttpStatus.BAD_REQUEST),

    /**
     * Kafka/FCM 관련 클라이언트 오류
     */
    JSON_PARSE_FAILED(false, 4502, "Kafka 메시지 JSON 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST),
    FCM_SEND_FAILED_PERMANENT(false, 4503, "FCM 전송에 실패했습니다. (영구 오류)", HttpStatus.BAD_REQUEST),

    /**
     * 3. 서버 에러 (5000~5999)
     */
    UNKNOWN_ERROR(false, 5500, "예기치 못한 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_ACCESS_FAILED(false, 5501, "Redis 처리 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * Kafka 전송/재시도 흐름 관련 서버 오류
     */
    KAFKA_PRODUCE_FAILED_PERMANENT(false, 5502, "Kafka 프로듀서 전송 실패 (영구 오류)", HttpStatus.INTERNAL_SERVER_ERROR),
    KAFKA_PRODUCE_FAILED_TRANSIENT(false, 5503, "Kafka 프로듀서 전송 실패 (일시적 오류)", HttpStatus.INTERNAL_SERVER_ERROR);


    private final boolean success;
    private final int code;
    private final String message;
    private HttpStatus httpStatus;

    NotificationResponseStatus(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    NotificationResponseStatus(boolean success, int code, String message, HttpStatus httpStatus) {
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
