package kr.ssok.notificationservice.global.exception;

import kr.ssok.common.exception.ResponseStatus;

/**
 * 일시적(Transient) 오류 발생 시 던지는 예외.
 * 예: FCM API 타임아웃, 일시적 네트워크 장애 등.
 * RetryTopicConfiguration에 의해 maxAttempts만큼 재시도 대상이 된다.
 */
public class NotificationTransientException extends NotificationException {
    public NotificationTransientException(ResponseStatus status) {
        super(status);
    }

    public NotificationTransientException(ResponseStatus status, Throwable cause) {
        super(status);
        initCause(cause);   // 생성자에서 initCause(cause)를 호출해, 실제 원인 예외 스택 트레이스를 보존
    }
}
