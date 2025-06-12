package kr.ssok.bluetoothservice.grpc.client;

import kr.ssok.bluetoothservice.client.dto.UserInfoDto;
import kr.ssok.common.grpc.user.UserIdRequest;
import kr.ssok.common.grpc.user.UserInfoResponse;
import kr.ssok.common.grpc.user.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceClient {
    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UserInfoDto getUserInfo(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        UserInfoResponse response =
                this.userServiceBlockingStub.getUserInfo(request);

        return UserInfoDto.builder()
                .username(response.getUsername())
                .phoneNumber(response.getPhoneNumber())
                .profileImage(response.getProfileImage())
                .build();
    }
}
