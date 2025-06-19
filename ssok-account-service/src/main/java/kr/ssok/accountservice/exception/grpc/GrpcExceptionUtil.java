package kr.ssok.accountservice.exception.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
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

    // gRPC 예외를 송금 서비스 전용 예외로 변환
    public static AccountException fromStatusRuntimeException(StatusRuntimeException e) {
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
            }

            return switch (code) {
                case 4050 -> new AccountException(AccountResponseStatus.USER_SERVER_ERROR);
                case 5011 -> new AccountException(AccountResponseStatus.USER_INFO_NOT_FOUND);
                default -> new AccountException(AccountResponseStatus.UNSUPPORTED_CODE);
            };
        }
        // 메타데이터 없거나 매핑 실패 시
        return new AccountException(AccountResponseStatus.GRPC_METADATA_INVALID);
    }
}
