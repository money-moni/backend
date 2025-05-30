package kr.ssok.bluetoothservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BluetoothTransferRequestDto {
    private String recvUuid;
    private Long sendAccountId;
    private Integer sendBankCode;
    private String sendName;
    private Long amount;
}
