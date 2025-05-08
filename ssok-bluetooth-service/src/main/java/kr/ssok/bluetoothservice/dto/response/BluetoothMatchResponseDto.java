package kr.ssok.bluetoothservice.dto.response;

import kr.ssok.bluetoothservice.client.dto.AccountInfoDto;
import kr.ssok.bluetoothservice.client.dto.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Bluetooth UUID 매칭 응답 DTO
 */
@Getter
@AllArgsConstructor
public class BluetoothMatchResponseDto {
    private List<UserInfoResponseDto> users;
    private AccountInfoDto primaryAccount;
}