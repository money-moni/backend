package kr.ssok.accountservice.dto.request.openbanking;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 서버에 계좌 잔액 조회 요청 시 사용되는 요청 DTO
 *
 * <p>계좌번호와 은행 코드를 포함하며,
 * 특정 계좌의 잔액을 조회할 수 있도록 구성됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAccountBalanceRequestDto {
    // 계좌번호
    private String accountNumber;
    // 은행 코드 (BankCode Enum의 idx 값)
    private int bankCode;

    /**
     * LinkedAccount 엔티티를 OpenBankingAccountBalanceRequestDto로 변환합니다.
     *
     * @param account 연동 계좌 엔티티
     * @return OpenBankingAccountBalanceRequestDto 객체
     */
    public static OpenBankingAccountBalanceRequestDto from(LinkedAccount account) {
        return OpenBankingAccountBalanceRequestDto.builder()
                .accountNumber(account.getAccountNumber())
                .bankCode(account.getBankCode().getIdx())
                .build();
    }

}
