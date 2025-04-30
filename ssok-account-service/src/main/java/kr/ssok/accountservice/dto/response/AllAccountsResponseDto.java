package kr.ssok.accountservice.dto.response;

import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 전체 계좌 목록 응답 DTO
 *
 * <p>오픈뱅킹으로부터 받은 계좌 정보를 프론트에 전달할 형식으로 가공합니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
public class AllAccountsResponseDto {
    private int bankCode;
    private String bankName;
    private String accountNumber;
    private String accountTypeCode;

    /**
     * OpenBankingAllAccountsResponseDto를 AllAccountsResponseDto로 변환합니다.
     *
     * @param openBankingAllAccountsResponse 오픈뱅킹 전체 계좌 응답 DTO
     * @return AllAccountsResponseDto 객체
     */
    public static AllAccountsResponseDto from(OpenBankingAllAccountsResponseDto openBankingAllAccountsResponse) {
        return AllAccountsResponseDto.builder()
                .bankCode(openBankingAllAccountsResponse.getBankCode())
                .bankName(BankCode.fromIdx(openBankingAllAccountsResponse.getBankCode()).getValue())
                .accountNumber(openBankingAllAccountsResponse.getAccountNumber())
                .accountTypeCode(AccountTypeCode.fromIdx(openBankingAllAccountsResponse.getAccountTypeCode()).getValue())
                .build();
    }
}
