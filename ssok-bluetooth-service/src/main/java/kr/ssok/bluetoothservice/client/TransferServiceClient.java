package kr.ssok.bluetoothservice.client;

import kr.ssok.bluetoothservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.bluetoothservice.dto.request.TransferRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.common.exception.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Transfer Service Feign Client
 */
@FeignClient(name = "ssok-transfer-service", url = "${external.transfer-service.url}")
public interface TransferServiceClient {

    /**
     * 블루투스 송금 요청
     *
     * @param senderUserId 요청자 userId
     * @param requestDto 블루투스 송금 요청 DTO
     * @return 송금 응답 DTO
     */
    @PostMapping("/api/transfers/openbank/bluetooth")
    BaseResponse<BluetoothTransferResponseDto> bluetoothTransfer(
            @RequestHeader("X-User-Id") Long senderUserId,
            @RequestBody TransferRequestDto requestDto
    );
}
