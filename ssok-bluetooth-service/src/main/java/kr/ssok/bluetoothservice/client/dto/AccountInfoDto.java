package kr.ssok.bluetoothservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 정보 DTO
 * - AccountServiceClient를 통해 가져오는 사용자 계좌 정보 데이터 구조
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoDto {
    private Long accountId;         // 주 계좌 id
    private String accountNumber;   // 주 계좌 번호
    private Integer bankCode;       // 은행 코드
    private Long balance;           // 계좌 잔액
}
