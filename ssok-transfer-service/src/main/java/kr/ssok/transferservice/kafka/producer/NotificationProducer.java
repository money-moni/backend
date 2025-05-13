package kr.ssok.transferservice.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.transferservice.kafka.message.KafkaNotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 알림용 Kafka 메시지 프로듀서
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
            log.error("Kafka 메시지 직렬화 실패", e);
        }
    }
}