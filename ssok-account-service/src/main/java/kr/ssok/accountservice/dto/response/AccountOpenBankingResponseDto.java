package kr.ssok.accountservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class AccountOpenBankingResponseDto {
    private String accountNumber;
    private int bankCode;
    private String bankName;
}
