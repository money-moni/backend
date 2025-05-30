package kr.ssok.bluetoothservice.service;

import kr.ssok.bluetoothservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothTransferResponseDto;

/**
 * 블루투스 송금 관리 비즈니스 로직 인터페이스
 */
public interface TransferService {
    BluetoothTransferResponseDto transfer(Long senderUserId, BluetoothTransferRequestDto requestDto);
}
