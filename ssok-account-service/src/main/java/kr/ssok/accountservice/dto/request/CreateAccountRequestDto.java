package kr.ssok.accountservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AccountRequestDto {
    private String accountNumber;
    private Long bankCode;
    private Long accountTypeCode;
}
