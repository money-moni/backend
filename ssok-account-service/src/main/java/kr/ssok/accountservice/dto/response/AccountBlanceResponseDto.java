package kr.ssok.accountservice.dto.response;

import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class AccountBlanceResponseDto {
    private Long accountId;
    private String accountNumber;
    private int bankCode;
    private String bankName;
    private String accountAlias;
    private boolean isPrimaryAccount;
    private AccountTypeCode accountTypeCode;
    private Long balance;
}
