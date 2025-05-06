package kr.ssok.bluetoothservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Bluetooth UUID 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BluetoothUuidRequestDto {
    private String bluetoothUUID; // 클라이언트에서 전달받는 Bluetooth UUID
}