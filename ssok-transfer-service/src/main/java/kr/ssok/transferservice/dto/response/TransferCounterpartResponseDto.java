package kr.ssok.transferservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 최근 송금 상대 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor // Projections.constructor 사용시 필요
public class TransferCounterpartResponseDto {
    private String counterpartName;            // 상대방 이름
    private String counterpartAccountNumber;   // 상대방 계좌 번호
    private LocalDateTime createdAt;           // 송금한 시각
}