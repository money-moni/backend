package kr.ssok.bluetoothservice.client;

import kr.ssok.bluetoothservice.client.dto.UserInfoDto;
import kr.ssok.common.exception.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ssok-user-service", url = "${external.user-service.url}") // url 임시 지정
public interface UserServiceClient {
    @GetMapping("/api/user/info")
    BaseResponse<UserInfoDto> getUserInfo(@RequestHeader("X-User-Id") String userId);
}