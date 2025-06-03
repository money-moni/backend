package kr.ssok.bluetoothservice.controller;

import kr.ssok.bluetoothservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.TransferService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bluetooth Transfer 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bluetooth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfers")
    public ResponseEntity<?> transferBluetoothByUuid(
            @RequestBody BluetoothTransferRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) {

        BluetoothTransferResponseDto response = transferService.transfer(Long.parseLong(userId), requestDto);

        return ResponseEntity.ok(new BaseResponse<>(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS, response));
    }
}
