package kr.ssok.userservice.client;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.userservice.dto.request.AligoVerificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.server.url:http://localhost:8083}")
public interface AligoClient {

    @PostMapping("/api/notification/verify")
    BaseResponse<Void> sendVerificationCode(@RequestBody AligoVerificationRequestDto requestDto);
}
