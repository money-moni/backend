package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.client.NotificationServiceClient;
import kr.ssok.transferservice.client.dto.request.FcmNotificationRequestDto;
import kr.ssok.transferservice.enums.TransferType;
import kr.ssok.transferservice.enums.BankCode;
import kr.ssok.transferservice.kafka.message.KafkaNotificationMessageDto;
import kr.ssok.transferservice.kafka.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 송금 알림을 전송하며, 실패 시 OpenFeign 기반 FCM 알림으로 fallback
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferNotificationSender {

    // Kafka 알림 프로듀서
    private final NotificationProducer notificationProducer;

    // Fallback용 OpenFeign 클라이언트
    private final NotificationServiceClient notificationServiceClient;

    /**
     * 송금 알림 전송 - Kafka → 실패 시 OpenFeign fallback
     *
     * @param userId 수신자 ID
     * @param accountId   수신자 계좌 ID
     * @param senderName 송신자 이름
     * @param bankCode 수신자 은행 코드
     * @param amount 송금 금액
     * @param type 송금 유형 (입금/출금)
     */
    public void sendKafkaNotification(Long userId, Long accountId, String senderName, Integer bankCode, Long amount, TransferType type) {
        KafkaNotificationMessageDto message = KafkaNotificationMessageDto.builder()
                .userId(userId)
                .accountId(accountId)
                .senderName(senderName)
                .bankCode(bankCode)
                .amount(amount)
                .transferType(type)
                .build();

        try {
            notificationProducer.send(message);
            log.info("Kafka 알림 전송 성공: {}", message);
        } catch (Exception e) {
            log.error("Kafka 알림 전송 실패: {}", e.getMessage());

            // Fallback to OpenFeign
            try {
                String title = String.format("%,d원 입금", amount);
                String bankName = BankCode.fromIdx(bankCode).getValue();
                String body = String.format("%s → 내 %s 통장", senderName, bankName);

                // data 맵 생성
                Map<String,String> data = Map.of(
                        "screen",    "AccountDetail",
                        "accountId", accountId.toString()
                );

                FcmNotificationRequestDto fallbackRequest = FcmNotificationRequestDto.builder()
                        .userId(userId)
                        .title(title)
                        .body(body)
                        .data(data)
                        .build();

                notificationServiceClient.sendFcmNotification(fallbackRequest);
                log.warn("OpenFeign Fallback 알림 전송 성공: {}", fallbackRequest);
            } catch (Exception feignException) {
                log.error("OpenFeign 알림 전송 실패: {}", feignException.getMessage());
            }
        }
    }
}