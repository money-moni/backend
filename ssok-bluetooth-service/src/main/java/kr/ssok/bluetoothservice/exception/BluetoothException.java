package kr.ssok.bluetoothservice.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;

/**
 * 송금 서비스 전용 예외 클래스
 */
public class BluetoothException extends BaseException {
    public BluetoothException(ResponseStatus status) {
        super(status);
    }
}
