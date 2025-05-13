package kr.ssok.notificationservice.domain.fcm.service;

/**
 * 알림 전송 서비스 인터페이스
 */
public interface NotificationService {
    void sendFcmNotification(Long userId, String title, String body);
}