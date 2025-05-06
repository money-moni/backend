package kr.ssok.bluetoothservice.controller;

import kr.ssok.bluetoothservice.dto.request.BluetoothUuidRequestDto;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.BluetoothService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bluetooth UUID 등록 컨트롤러
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
        bluetoothService.registerBluetoothUuid(Long.parseLong(userId), requestDto.getBluetoothUUID());
        return ResponseEntity.ok(new BaseResponse<>(BluetoothResponseStatus.REGISTER_SUCCESS));
    }
}
