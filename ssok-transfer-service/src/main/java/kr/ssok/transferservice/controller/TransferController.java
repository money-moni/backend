package kr.ssok.transferservice.controller;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * 송금 요청을 처리하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/transfers/openbank")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * 일반 송금 API 요청 처리
     *
     * @param requestDto 클라이언트에서 받은 송금 요청 DTO
     * @param userId     Gateway에서 전달한 사용자 ID (헤더)
     * @return CompletableFuture<ResponseEntity> 형태의 비동기 응답 - 송금 결과를 담은 BaseResponse 객체
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<BaseResponse<TransferResponseDto>>> transfer(
            @RequestBody TransferRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) {

        // 서비스 호출 후 완료 시점에 BaseResponse를 감싸서 반환
        return transferService
                .transfer(Long.parseLong(userId), requestDto, TransferMethod.GENERAL)
                .thenApply(result ->
                        ResponseEntity.ok(new BaseResponse<>(TransferResponseStatus.TRANSFER_SUCCESS, result))
                );
    }

    /**
     * 블루투스 송금 API
     *
     * @param requestDto 블루투스 송금 요청 DTO
     * @param userId     요청자 유저 ID (헤더로 전달)
     * @return CompletableFuture<ResponseEntity> 형태의 비동기 응답 - 송금 결과를 담은 BaseResponse 객체
     */
    @PostMapping("/bluetooth")
    public CompletableFuture<ResponseEntity<BaseResponse<BluetoothTransferResponseDto>>> bluetoothTransfer(
            @RequestBody BluetoothTransferRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId) {

        // 서비스 호출 후 완료 시점에 BaseResponse를 감싸서 반환
        return transferService
                .bluetoothTransfer(Long.parseLong(userId), requestDto, TransferMethod.BLUETOOTH)
                .thenApply(result ->
                        ResponseEntity.ok(new BaseResponse<>(TransferResponseStatus.TRANSFER_SUCCESS, result))
                );
    }
}
