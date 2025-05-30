package kr.ssok.bluetoothservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    private Long sendAccountId;        // 출금 계좌 ID
    private Integer sendBankCode;      // 출금 은행 코드
    private String sendName;           // 출금자 이름
    private Long recvUserId;           // 입금 유저 id
    private Long amount;               // 송금 금액
}
