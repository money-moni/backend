package kr.ssok.accountservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccountOpenBankingResponseDto {
    private String accountNumber;
    private int bankCode;
    private String bankName;
}
