package kr.ssok.transferservice.dto.response;

import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 최근 송금 이력 응답 DTO
 */
@Getter
@Builder
public class TransferRecentHistoryResponseDto {

    private Long transferId;                     // 송금 ID
    private TransferType transferType;           // 송금 유형 (출금 / 입금)
    private String counterpartName;              // 상대 이름
    private Long transferMoney;                  // 금액
    private CurrencyCode currencyCode;           // 통화 코드
    private TransferMethod transferMethod;       // 송금 방식
    private LocalDateTime createdAt;             // 송금 시간
}