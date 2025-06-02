package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.kafka.message.KafkaNotificationMessageDto;
import kr.ssok.transferservice.kafka.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferNotificationSender {

    // Kafka 알림 프로듀서
    private final NotificationProducer notificationProducer;

    public void sendKafkaNotification(Long userId, String senderName, Integer bankCode, Long amount, TransferType type) {
        KafkaNotificationMessageDto message = KafkaNotificationMessageDto.builder()
                .userId(userId)
                .senderName(senderName)
                .bankCode(bankCode)
                .amount(amount)
                .transferType(type)
                .build();
        notificationProducer.send(message);
    }
}