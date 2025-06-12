package kr.ssok.userservice.exception.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import kr.ssok.common.exception.ResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcExceptionUtil {
    private static final Metadata.Key<String> CODE_KEY =
            Metadata.Key.of("code", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> CLASS_KEY =
            Metadata.Key.of("class", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> MESSAGE_KEY =
            Metadata.Key.of("message", Metadata.ASCII_STRING_MARSHALLER);

    // 송금 서비스의 에러코드를 gRPC StatusRuntimeException으로 변환
    public static StatusRuntimeException toStatusRuntimeException(ResponseStatus status) {
        Metadata metadata = new Metadata();
        metadata.put(CODE_KEY, String.valueOf(status.getCode())); // int → String
        metadata.put(CLASS_KEY, status.getClass().getSimpleName());
        metadata.put(MESSAGE_KEY, status.getMessage());

        return Status.fromCodeValue(
                        status.getHttpStatus() != null ? status.getHttpStatus().value() : 400)
                .withDescription(status.getMessage())
                .asRuntimeException(metadata);
    }
}
