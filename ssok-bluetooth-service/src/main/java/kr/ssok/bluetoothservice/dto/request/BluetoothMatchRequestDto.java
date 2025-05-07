package kr.ssok.bluetoothservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bluetooth UUID 매칭 요청 DTO
 */
@Getter
@NoArgsConstructor
public class BluetoothMatchRequestDto {
    private List<String> bluetoothUUIDs;
}