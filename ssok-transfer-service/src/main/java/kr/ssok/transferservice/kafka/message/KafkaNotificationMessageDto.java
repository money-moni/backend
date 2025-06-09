package kr.ssok.transferservice.kafka.message;

import kr.ssok.transferservice.enums.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka로 전송될 알림 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaNotificationMessageDto {
    private Long userId;               // 수신자 userId
    private String senderName;         // 송신자 이름
    private Integer bankCode;          // 수신자 은행 코드
    private Long amount;               // 금액
    private TransferType transferType; // 송금 유형
}