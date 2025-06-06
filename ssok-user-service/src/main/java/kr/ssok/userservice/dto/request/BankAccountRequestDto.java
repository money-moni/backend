package kr.ssok.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountRequestDto {
    private String username;
    private String phoneNumber;
    private int accountTypeCode;
    private String userTypeCode;
}