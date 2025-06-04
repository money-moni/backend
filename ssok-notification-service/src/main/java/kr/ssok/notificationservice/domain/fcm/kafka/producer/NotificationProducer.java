package kr.ssok.notificationservice.domain.fcm.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.notificationservice.domain.fcm.kafka.message.KafkaNotificationMessageDto;
import kr.ssok.notificationservice.global.exception.NotificationPermanentException;
import kr.ssok.notificationservice.global.exception.NotificationResponseStatus;
import kr.ssok.notificationservice.global.exception.NotificationTransientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 알림용 Kafka 메시지 프로듀서
 * - 메시지를 JSON으로 직렬화하고, 예외가 발생하면 Transient/Permanent 예외로 구분해 던진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.notification-topic}")
    private String topic;

    /**
     * 알림 메시지를 Kafka 토픽으로 발행
     *
     * @param message 알림 내용
     */
    public void send(KafkaNotificationMessageDto message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, jsonMessage); // Kafka에 메시지를 비동기로 전송
            log.info("Kafka 알림 메시지 발행: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 → Permanent 오류
            log.error("Kafka 메시지 직렬화 실패", e);
            throw new NotificationPermanentException(
                    NotificationResponseStatus.JSON_PARSE_FAILED, e);
        } catch (Exception e) {
            // Kafka 전송 중 일시적 장애(Timeout, Broker 연결 실패 등) → Transient 오류
            log.error("Kafka 메시지 발행 중 일시적 오류 발생", e);
            throw new NotificationTransientException(
                    NotificationResponseStatus.KAFKA_PRODUCE_FAILED_TRANSIENT, e);
        }
    }
}