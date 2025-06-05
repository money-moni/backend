package kr.ssok.notificationservice.domain.fcm.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.notificationservice.domain.fcm.enums.BankCode;
import kr.ssok.notificationservice.global.exception.NotificationPermanentException;
import kr.ssok.notificationservice.global.exception.NotificationTransientException;
import kr.ssok.notificationservice.global.exception.NotificationResponseStatus;
import kr.ssok.notificationservice.domain.fcm.kafka.message.KafkaNotificationMessageDto;
import kr.ssok.notificationservice.domain.fcm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 알림 메시지 수신자
 * 1) Main 토픽(consumption)용 리스너
 * 2) Recovery 토픽(복구)용 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fcm.image-url}")
    private String imageUrl;

    /**
     * Main 토픽에서 메시지를 수신 후 FCM 푸시 알림 전송
     *
     * @param messageJson Kafka로부터 받은 JSON 문자열
     */
    @KafkaListener(
            topics = "${kafka.notification-topic}",
            groupId = "${kafka.group-id}",
            containerFactory = "mainKafkaListenerContainerFactory"  // Main 전용 컨테이너
    )
    public void consume(String messageJson) {
        KafkaNotificationMessageDto message;
        try {
            // 1) JSON 파싱
            message = objectMapper.readValue(messageJson, KafkaNotificationMessageDto.class);
        } catch (Exception e) {
            // JSON 파싱 실패 → Permanent 오류
            log.error("consume(): JSON 파싱 실패 – Permanent 예외로 처리", e);
            throw new NotificationPermanentException(
                    NotificationResponseStatus.JSON_PARSE_FAILED, e);
        }

        try {
            // 2) FCM 푸시 전송 로직
            String title = String.format("%,d원 입금", message.getAmount());
            String bankName = BankCode.fromIdx(message.getBankCode()).getValue();
            String body = String.format("%s → 내 %s 통장", message.getSenderName(), bankName);

            notificationService.sendFcmNotification(message.getUserId(), title, body);

            log.info("consume(): FCM 알림 전송 성공 (userId={}, amount={})",
                    message.getUserId(), message.getAmount());

        } catch (NotificationPermanentException pe) {
            // FCM 전송 중 Permanent 예외 → 즉시 DLT
            log.error("consume(): Permanent 예외 발생, 즉시 DLT – {}", pe.getMessage());
            throw pe;
        } catch (Exception e) {
            // 그 외 예외 → Transient 오류 (재시도 대상)
            log.error("consume(): Transient 예외 발생, 재시도 대상 – {}", e.getMessage(), e);
            throw new NotificationTransientException(
                    NotificationResponseStatus.FCM_SEND_FAILED_PERMANENT, e);
        }
    }

    /**
     * Recovery 토픽에서 들어온 메시지를 재소비(한 번만)하는 메서드
     *
     * @param messageJson 복구 토픽으로부터 받은 JSON 문자열
     */
    @KafkaListener(
            topics = "${kafka.notification-recover-topic}",
            groupId = "${kafka.recovery-group-id}",
            containerFactory = "recoveryKafkaListenerContainerFactory"  // Recovery 전용 컨테이너
    )
    public void reconsumeFailedMessages(String messageJson) {
        log.info("reconsumeFailedMessages(): 복구 토픽 메시지 수신, messageJson={}", messageJson);
        try {
            consume(messageJson);
        } catch (Exception e) {
            // Recovery 단계에서 예외 발생 시 swallow(무시)하고 로그만 남김 → 무한루프 방지
            log.error("reconsumeFailedMessages(): 복구 단계에서 예외 발생. swallow 처리 후 종료. message={}", messageJson, e);
        }
    }
}
