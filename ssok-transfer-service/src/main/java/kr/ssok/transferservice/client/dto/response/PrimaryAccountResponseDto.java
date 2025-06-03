package kr.ssok.transferservice.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryAccountResponseDto {
    private Long accountId;
    private String accountNumber;
    private Integer bankCode;
    private String username;
}
