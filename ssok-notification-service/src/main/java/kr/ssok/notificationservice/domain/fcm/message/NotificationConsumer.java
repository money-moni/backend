package kr.ssok.notificationservice.domain.fcm.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.notificationservice.domain.fcm.enums.BankCode;
import kr.ssok.notificationservice.domain.fcm.service.NotificationService;
import kr.ssok.notificationservice.domain.fcm.dto.message.KafkaNotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 알림 메시지 수신자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 이미지 처리는 이후 s3 적용 이후 진행
    @Value("${fcm.image-url}")
    private String imageUrl;

    /**
     * Kafka 메시지 수신 후 FCM 푸시 알림 전송
     */
    @KafkaListener(topics = "${kafka.notification-topic}", groupId = "${kafka.group-id}")
    public void consume(String messageJson) {
        try {
            KafkaNotificationMessageDto message = objectMapper.readValue(messageJson, KafkaNotificationMessageDto.class);
            String title = String.format("%,d원 입금", message.getAmount());

            String bankName = BankCode.fromIdx(message.getBankCode()).getValue();
            String body = String.format("%s → 내 %s 통장", message.getSenderName(), bankName);

            notificationService.sendFcmNotification(message.getUserId(), title, body);
        } catch (Exception e) {
            log.error("Kafka 메시지 수신 또는 처리 실패", e);
        }
    }
}