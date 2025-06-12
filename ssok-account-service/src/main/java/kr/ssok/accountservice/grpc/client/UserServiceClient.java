package kr.ssok.accountservice.grpc.client;

import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.common.grpc.user.UserIdRequest;
import kr.ssok.common.grpc.user.UserInfoResponse;
import kr.ssok.common.grpc.user.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UserInfoResponseDto getUserInfo(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        UserInfoResponse response =
                this.userServiceBlockingStub.getUserInfo(request);

        return UserInfoResponseDto.builder()
                .username(response.getUsername())
                .phoneNumber(response.getPhoneNumber())
                .build();
    }
}
