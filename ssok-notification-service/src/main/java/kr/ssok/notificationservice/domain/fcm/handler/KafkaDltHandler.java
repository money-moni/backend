package kr.ssok.notificationservice.domain.fcm.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * DLQ로 전송된 메시지에 대한 후처리를 담당하는 클래스
 *
 * 이 클래스는 RetryTopicConfigurationBuilder에서 지정한 dltHandlerMethod로 호출
 */
@Slf4j
@Component
public class KafkaDltHandler {

    /**
     * DLQ 메시지를 후처리하는 메서드
     *
     * @param record         Kafka 메시지 레코드 전체
     * @param topic          원본 Kafka 토픽 이름
     * @param partition      메시지가 수신된 파티션 번호
     * @param offset         메시지 오프셋
     * @param errorMessage   예외 메시지
     * @param groupId        Kafka 그룹 ID
     */
    public void handleDltMessage(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage,
            @Header(KafkaHeaders.GROUP_ID) String groupId
    ) {
        log.error("[DLQ 처리] 실패 메시지: '{}', 토픽: '{}', 파티션: {}, 오프셋: {}, 그룹: {}, 예외: {}",
                record.value(), topic, partition, offset, groupId, errorMessage);

        // TODO: 실패 메시지 Slack 알림 및 로그 전송으로 확장 예정
    }
}
