package kr.ssok.notificationservice.domain.fcm.service.Impl;

import kr.ssok.notificationservice.domain.fcm.client.FirebaseClient;
import kr.ssok.notificationservice.domain.fcm.dto.request.FcmMessageRequestDto;
import kr.ssok.notificationservice.global.exception.NotificationException;
import kr.ssok.notificationservice.global.exception.NotificationResponseStatus;
import kr.ssok.notificationservice.domain.fcm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * FCM 푸시 알림 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final FirebaseClient firebaseClient;

    @Value("${fcm.image-url}")
    private String defaultImage;

    @Override
    public void sendFcmNotification(Long userId, String title, String body, Map<String,String> data) {
        try {
            String token = redisTemplate.opsForValue().get("userfcm:" + userId);

            if (token == null) {
                log.warn("FCM 토큰이 존재하지 않습니다: userId={}", userId);
                return;
            }

            // FCM 메시지 요청 생성
            FcmMessageRequestDto request = FcmMessageRequestDto.builder()
                    .title(title)
                    .body(body)
                    .image(defaultImage)
                    .token(token)
                    .data(data)
                    .build();

            // FCM 클라이언트로 메시지 전송
            firebaseClient.sendNotification(request);
        } catch (DataAccessException e) {
            throw new NotificationException(NotificationResponseStatus.REDIS_ACCESS_FAILED);
        }
    }
}