package kr.ssok.notificationservice.global.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;

/**
 * 알림 서비스 전용 예외 클래스
 */
public class NotificationException extends BaseException {
    public NotificationException(ResponseStatus status) {
        super(status);
    }
}
