package kr.ssok.notificationservice.global.exception;

import kr.ssok.common.exception.ResponseStatus;

/**
 * 영구적(Permanent) 오류 발생 시 던지는 예외.
 * 예: JSON 파싱 오류 등, 재시도해도 소용이 없는 오류.
 * RetryTopicConfigurationBuilder가 이 예외를 감지하면 즉시 DLT로 보낸다.
 */
public class NotificationPermanentException extends NotificationException {
    public NotificationPermanentException(ResponseStatus status) {
        super(status);
    }

    public NotificationPermanentException(ResponseStatus status, Throwable cause) {
        super(status);
        initCause(cause);
    }
}
