package kr.ssok.userservice.grpc.server;

import io.grpc.stub.StreamObserver;
import kr.ssok.common.grpc.user.UserIdRequest;
import kr.ssok.common.grpc.user.UserInfoResponse;
import kr.ssok.common.grpc.user.UserServiceGrpc;
import kr.ssok.userservice.dto.response.UserInfoResponseDto;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.exception.grpc.GrpcExceptionUtil;
import kr.ssok.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGrpcInternalServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    @Override
    public void getUserInfo(UserIdRequest request, StreamObserver<UserInfoResponse> responseObserver) {
        try {
            UserInfoResponseDto result =
                    this.userService.getUserInfo(Long.parseLong(request.getUserId()));

            UserInfoResponse response = UserInfoResponse.newBuilder()
                    .setUsername(result.getUsername())
                    .setPhoneNumber(result.getPhoneNumber())
                    .setProfileImage(result.getProfileImage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("정상적으로 onCompleted - userId: {}", request.getUserId());
        } catch (UserException ex) {
            log.error("[gRPC][getUserInfo] UserException: {}", ex.getStatus(), ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            log.error("[gRPC][getUserInfo] Unexpected error", ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(UserResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
