package kr.ssok.accountservice.dto.request.openbanking;

import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 서버에 계좌 실명(예금주) 조회 요청 시 사용되는 요청 DTO
 *
 * <p>계좌번호와 은행 코드를 포함하며,
 * 예금주의 실명 정보를 확인할 수 있도록 구성됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAccountOwnerRequestDto {
    // 계좌번호
    private String accountNumber;
    // 은행 코드 (BankCode Enum의 idx 값)
    private int bankCode;

    /**
     * AccountOwnerRequestDto를 OpenBankingAccountOwnerRequestDto로 변환합니다.
     *
     * @param accountOwnerRequest 실명 조회 요청 DTO
     * @return OpenBankingAccountOwnerRequestDto 객체
     */
    public static OpenBankingAccountOwnerRequestDto from(AccountOwnerRequestDto accountOwnerRequest) {
        return OpenBankingAccountOwnerRequestDto.builder()
                .accountNumber(accountOwnerRequest.getAccountNumber())
                .bankCode(accountOwnerRequest.getBankCode())
                .build();
    }
}
