package kr.ssok.transferservice.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 오픈뱅킹에 요청할 송금 요청 DTO
 */
@Builder
@Getter
public class OpenBankingTransferRequestDto {
    private String sendAccountNumber;
    private Integer sendBankCode;
    private String sendName;
    private String recvAccountNumber;
    private Integer recvBankCode;
    private String recvName;
    private Long amount;
}