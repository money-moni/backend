package kr.ssok.transferservice.dto.response;

import kr.ssok.transferservice.enums.BankCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 최근 송금 상대 응답 DTO
 */
@Getter
public class TransferCounterpartResponseDto {
    private final String counterpartName;            // 상대방 이름
    private final String counterpartAccountNumber;   // 상대방 계좌 번호
    private final Integer counterpartBankCode;      // 상대방 계좌 은행 코드
    private final LocalDateTime createdAt;           // 송금한 시각

    /**
     * QueryDSL Projections.constructor 용 생성자
     * @param counterpartName          상대방 이름
     * @param counterpartAccountNumber 상대방 계좌 번호
     * @param counterpartBankCode      BankCode enum 으로 받아와서 getIdx() 호출
     * @param createdAt                송금 시각
     */
    public TransferCounterpartResponseDto(
            String counterpartName,
            String counterpartAccountNumber,
            BankCode counterpartBankCode,
            LocalDateTime createdAt) {
        this.counterpartName          = counterpartName;
        this.counterpartAccountNumber = counterpartAccountNumber;
        this.counterpartBankCode      = counterpartBankCode.getIdx();
        this.createdAt                = createdAt;
    }
}