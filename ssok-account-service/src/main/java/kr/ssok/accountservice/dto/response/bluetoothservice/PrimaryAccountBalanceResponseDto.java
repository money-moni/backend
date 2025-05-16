package kr.ssok.accountservice.dto.response.bluetoothservice;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 블루투스 서비스 등 내부 시스템 간 통신 시 사용자 주계좌 잔액 정보를 제공하는 응답 DTO
 *
 * <p>계좌 ID, 계좌 번호, 은행 코드, 계좌 잔액을 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용이며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryAccountBalanceResponseDto {
    private Long accountId;
    private String accountNumber;
    private int bankCode;
    private Long balance;

    /**
     * LinkedAccount 엔티티와 사용자 이름을 기반으로 PrimaryAccountBalanceResponseDto 변환합니다.
     *
     * @param account 주계좌로 설정된 LinkedAccount 엔티티
     * @param balance 계좌 잔액
     * @return PrimaryAccountBalanceResponseDto 객체
     */
    public static PrimaryAccountBalanceResponseDto from(LinkedAccount account, Long balance) {
        return PrimaryAccountBalanceResponseDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .bankCode(account.getBankCode().getIdx())
                .balance(balance)
                .build();
    }
}
