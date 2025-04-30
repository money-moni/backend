package kr.ssok.userservice.client;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.userservice.dto.request.AligoVerificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 알리고 SMS 서비스를 호출하기 위한 Feign Client 인터페이스
 * notification-service의 API를 호출하여 SMS 발송 기능을 사용합니다.
 */
@FeignClient(name = "notification-service", url = "${notification.server.url:http://localhost:8084}")
public interface AligoClient {

    /**
     * 인증코드 SMS 발송 요청
     * notification-service의 API를 호출하여 SMS 발송을 요청합니다.
     * 
     * @param requestDto 휴대폰 번호와 인증코드가 포함된 요청 DTO
     * @return SMS 발송 결과
     */
    @PostMapping("/api/notification/verify")
    BaseResponse<Void> sendVerificationCode(@RequestBody AligoVerificationRequestDto requestDto);
}
