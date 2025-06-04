package kr.ssok.transferservice.dto.response;

import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 송금 이력 조회 API 응답 DTO
 */
@Getter
@Builder
public class TransferHistoryResponseDto {
    private Long transferId;                // 송금 이력 ID
    private TransferType transferType;      // 송금 유형 (WITHDRAWAL, DEPOSIT)
    private Long transferMoney;             // 송금 금액
    private CurrencyCode currencyCode;      // 통화 코드 (KRW, USD)
    private TransferMethod transferMethod;  // 송금 방식 (GENERAL, BLUETOOTH)
    private String counterpartAccount;      // 상대방 계좌번호
    private String counterpartName;         // 상대방 이름
    private LocalDateTime createdAt;        // 거래 생성 시각
}
