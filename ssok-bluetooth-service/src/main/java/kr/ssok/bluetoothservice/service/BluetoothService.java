package kr.ssok.bluetoothservice.service;

/**
 * 블루투스 UUID 관리 비즈니스 로직 인터페이스
 */
public interface BluetoothService {
    void registerBluetoothUuid(Long userId, String bluetoothUUID);
}
