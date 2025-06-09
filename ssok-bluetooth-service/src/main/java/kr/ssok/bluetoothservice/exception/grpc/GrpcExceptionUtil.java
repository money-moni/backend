package kr.ssok.bluetoothservice.exception.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcExceptionUtil {
    private static final Metadata.Key<String> CODE_KEY =
            Metadata.Key.of("code", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> CLASS_KEY =
            Metadata.Key.of("class", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> MESSAGE_KEY =
            Metadata.Key.of("message", Metadata.ASCII_STRING_MARSHALLER);

    // gRPC 예외를 송금 서비스 전용 예외로 변환
    public static BluetoothException fromStatusRuntimeException(StatusRuntimeException e) {
        Metadata metadata = Status.trailersFromThrowable(e);
        if (metadata != null) {
            String codeStr = metadata.get(CODE_KEY);
            String className = metadata.get(CLASS_KEY);
            String message = metadata.get(MESSAGE_KEY);

            log.warn("gRPC Error - class: {}, code: {}, message: {}", className, codeStr, message);

            int code = -1;
            try {
                code = Integer.parseInt(codeStr);
            } catch (Exception ex) {
                log.error("gRPC code 파싱 에러: {}", codeStr, ex);
                return new BluetoothException(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS);
            }

            return switch (code) {
                case 3123 -> new BluetoothException(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS);
                case 132123 -> new BluetoothException(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS);
                default -> new BluetoothException(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS);
            };
        }
        // 메타데이터 없거나 매핑 실패 시
        return new BluetoothException(BluetoothResponseStatus.BLUETOOTH_TRANSFER_SUCCESS);
    }
}
