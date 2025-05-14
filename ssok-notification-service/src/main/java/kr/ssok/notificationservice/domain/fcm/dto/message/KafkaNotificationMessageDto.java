package kr.ssok.notificationservice.domain.fcm.dto.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka로 전달된 알림 메시지 DTO
 */
@Getter
@NoArgsConstructor
public class KafkaNotificationMessageDto {
    private Long userId;
    private String senderName;
    private Integer bankCode;
    private Long amount;
    private String transferType;
}