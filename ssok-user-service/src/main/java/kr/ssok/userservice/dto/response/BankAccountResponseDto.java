package kr.ssok.userservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountResponseDto {
    private String accountNumber;
    private String status;
    private String message;
}