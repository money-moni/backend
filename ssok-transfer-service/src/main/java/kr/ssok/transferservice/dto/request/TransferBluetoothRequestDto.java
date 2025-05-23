package kr.ssok.transferservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 블루투스 송금 요청 DTO
 * - 클라이언트가 서버로 보낼 JSON 요청 형식
 * - 불변 객체로 설계되어 안전함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferBluetoothRequestDto {

    private Long sendAccountId;        // 출금 계좌 ID
    private Integer sendBankCode;      // 출금 은행 코드
    private String sendName;           // 출금자 이름
    private Long recvAccountId;        // 입금 계좌 ID
    private String recvAccountNumber;  // 입금 계좌번호
    private Integer recvBankCode;      // 입금 은행 코드
    private String recvName;           // 입금 상대방 이름
    private Long amount;               // 송금 금액
}
