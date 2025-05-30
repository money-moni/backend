package kr.ssok.bluetoothservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BluetoothTransferResponseDto {
    private Long sendAccountId;
    private String recvName;
    private Long amount;
}