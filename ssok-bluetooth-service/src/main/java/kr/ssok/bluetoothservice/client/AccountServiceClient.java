package kr.ssok.bluetoothservice.client;

import kr.ssok.bluetoothservice.client.dto.AccountInfoDto;
import kr.ssok.common.exception.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 계좌 서비스와 통신하는 Feign 클라이언트 인터페이스
 * feign 통신의 에러 처리는 이후에 고려
 */
@FeignClient(name = "account-service", url = "${external.account-service.url}") // url 임시 지정
public interface AccountServiceClient {
    @GetMapping("/api/account/primary")
    BaseResponse<AccountInfoDto> getPrimaryAccount(@RequestHeader("X-User-Id") String userId);
}
