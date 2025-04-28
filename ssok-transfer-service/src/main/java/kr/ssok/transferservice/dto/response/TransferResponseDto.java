package kr.ssok.transferservice.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 송금 응답 결과로 클라이언트에게 전달되는 실제 데이터
 */
@Getter
@Builder
public class TransferResponseDto {

    private final Long sendAccountId;
    private final String recvAccountNumber;
    private final Long amount;
}