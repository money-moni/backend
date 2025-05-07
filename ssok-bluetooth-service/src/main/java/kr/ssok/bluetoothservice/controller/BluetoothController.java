package kr.ssok.bluetoothservice.controller;

import kr.ssok.bluetoothservice.dto.request.BluetoothMatchRequestDto;
import kr.ssok.bluetoothservice.dto.request.BluetoothUuidRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothMatchResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.BluetoothService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bluetooth 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bluetooth")
public class BluetoothController {

    private final BluetoothService bluetoothService;

    /**
     * Bluetooth UUID 등록 API
     * - 헤더의 사용자 ID와 Body의 UUID를 Redis에 등록
     *
     * @param requestDto Bluetooth UUID
     * @param userId 사용자 ID (X-User-Id 헤더)
     */
    @PostMapping("/uuid")
    public ResponseEntity<?> registerUuid(
            @RequestBody BluetoothUuidRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) {
        this.bluetoothService.registerBluetoothUuid(Long.parseLong(userId), requestDto.getBluetoothUUID());
        return ResponseEntity.ok(new BaseResponse<>(BluetoothResponseStatus.REGISTER_SUCCESS));
    }

    /**
     * 주변 Bluetooth UUID 목록을 기반으로 매칭된 사용자 정보 조회 API
     *
     * @param requestDto BluetoothMatchRequestDto
     * @return 매칭된 사용자 정보와 주 계좌 정보
     */
    @PostMapping("/match")
    public ResponseEntity<BaseResponse<BluetoothMatchResponseDto>> matchBluetooth(
            @RequestBody BluetoothMatchRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) {
        BluetoothMatchResponseDto responseDto = bluetoothService.matchBluetoothUsers(userId, requestDto.getBluetoothUUIDs());
        return ResponseEntity.ok(new BaseResponse<>(BluetoothResponseStatus.MATCH_SUCCESS, responseDto));
    }
}
