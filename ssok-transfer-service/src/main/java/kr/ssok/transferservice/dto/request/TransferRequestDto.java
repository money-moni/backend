package kr.ssok.transferservice.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 송금 요청 DTO
 * - 클라이언트가 서버로 보낼 JSON 요청 형식
 * - 불변 객체로 설계되어 안전함
 */
@Getter
@Builder
public class TransferRequestDto {

    private final Long sendAccountId;        // 출금 계좌 ID
    private final Integer sendBankCode;      // 출금 은행 코드
    private final String sendName;           // 출금자 이름
    private final String recvAccountNumber;  // 입금 계좌번호
    private final Integer recvBankCode;      // 입금 은행 코드
    private final String recvName;           // 입금 상대방 이름
    private final Long amount;               // 송금 금액

}