package kr.ssok.notificationservice.domain.fcm.service;

/**
 * FCM 토큰 관리 인터페이스
 */
public interface FcmService {
    void registerFcmToken(Long userId, String token);
}