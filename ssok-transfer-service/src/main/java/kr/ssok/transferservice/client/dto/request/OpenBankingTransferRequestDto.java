package kr.ssok.transferservice.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹에 요청할 송금 요청 DTO
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingTransferRequestDto {
    private String sendAccountNumber;
    private Integer sendBankCode;
    private String sendName;
    private String recvAccountNumber;
    private Integer recvBankCode;
    private String recvName;
    private Long amount;
}