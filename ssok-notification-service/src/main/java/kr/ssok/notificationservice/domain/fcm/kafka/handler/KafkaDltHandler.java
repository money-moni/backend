package kr.ssok.notificationservice.domain.fcm.kafka.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * DLT(Dead Letter Topic)로 전송된 메시지에 대한 후처리를 담당하는 클래스
 * - 실패 메시지를 "Recovery Topic"으로 재발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDltHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.notification-recover-topic}")
    private String RECOVERY_TOPIC;

    /**
     * DLT 메시지를 후처리하여, 복구 토픽으로 재전송
     *
     * @param record        DLT로 온 ConsumerRecord
     * @param topic         DLT 토픽 이름 (예: notification-topic-dlt)
     * @param partition     파티션 번호
     * @param offset        오프셋 번호
     * @param errorMessage  발생했던 예외 메시지
     * @param groupId       컨슈머 그룹 ID
     */
    public void handleDltMessage(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage,
            @Header(KafkaHeaders.GROUP_ID) String groupId
    ) {
        log.warn("[DLQ 처리] 실패 메시지: '{}', 토픽: '{}', 파티션: {}, 오프셋: {}, 그룹: {}, 예외: {}",
                record.value(), topic, partition, offset, groupId, errorMessage);

        // 2) 복구 토픽으로 메시지 재전송 (비동기)
        kafkaTemplate.send(RECOVERY_TOPIC, record.value())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[DLQ 처리] 복구 토픽 전송 실패: {}", RECOVERY_TOPIC, ex);
                    } else {
                        log.info("[DLQ 처리] 복구 토픽 전송 성공: {}", RECOVERY_TOPIC);
                    }
                });
    }
}
