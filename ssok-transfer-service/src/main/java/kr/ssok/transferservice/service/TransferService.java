package kr.ssok.transferservice.service;

import kr.ssok.transferservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.enums.TransferMethod;

/**
 * 송금 처리 비즈니스 로직 인터페이스
 */
public interface TransferService {

    /**
     * 송금 요청을 처리
     *
     * @param userId    사용자 ID (Gateway 헤더에서 전달됨)
     * @param requestDto 송금 요청 DTO
     * @return 송금 처리 결과 DTO
     */
    TransferResponseDto transfer(Long userId, TransferRequestDto requestDto, TransferMethod transferMethod);

    BluetoothTransferResponseDto bluetoothTransfer(Long userId, BluetoothTransferRequestDto requestDto, TransferMethod transferMethod);
}
