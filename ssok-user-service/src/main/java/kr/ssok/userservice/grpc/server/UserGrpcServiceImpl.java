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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
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
        } catch (UserException ex) {
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(UserResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
