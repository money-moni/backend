package kr.ssok.notificationservice.domain.fcm.client;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import kr.ssok.notificationservice.domain.fcm.dto.request.FcmMessageRequestDto;
import kr.ssok.notificationservice.global.exception.NotificationPermanentException;
import kr.ssok.notificationservice.global.exception.NotificationResponseStatus;
import kr.ssok.notificationservice.global.exception.NotificationTransientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Firebase 클라이언트
 */
@Slf4j
@Component
public class FirebaseClient {

    /**
     * FCM 알림 전송
     *
     * @param request FCM 알림 요청 DTO
     */
    public void sendNotification(FcmMessageRequestDto request) {
        // data null 대비
        Map<String, String> dataMap = request.getData() != null
                ? request.getData()
                : Collections.emptyMap();

        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setImage(request.getImage())
                        .build())
                .putAllData(dataMap)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 알림 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("푸시 알림 전송 실패: {}", e.getMessage(), e);
            if (e.getMessage().contains("Requested entity was not found")) {
                throw new NotificationPermanentException(
                        NotificationResponseStatus.FCM_TOKEN_INVALID, e);
            }
            throw new NotificationTransientException(
                    NotificationResponseStatus.FCM_SEND_FAILED_TRANSIENT, e);
        } catch (Exception e) {
            log.error("푸시 알림 중 알 수 없는 오류", e);
            throw new NotificationTransientException(
                    NotificationResponseStatus.FCM_SEND_FAILED_TRANSIENT, e);
        }
    }
}