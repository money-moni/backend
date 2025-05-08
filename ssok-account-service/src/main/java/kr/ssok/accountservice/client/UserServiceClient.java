package kr.ssok.accountservice.client;

import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.common.exception.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 내부 유저 서비스 서버와의 통신을 위한 Feign Client 인터페이스
 *
 * <p>사용자 ID를 기반으로 유저 정보를 조회합니다.</p>
 *
 * <p>추후 해당 Client는 gRPC 기반 통신으로 대체될 예정</p>
 */
@FeignClient(name = "user-service", url = "${external.user-service.url}") // name 임시 지정, url은 실행을 위해 임시로 지정
public interface UserServiceClient {

    /**
     * 사용자 ID에 해당하는 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보가 포함된 {@link BaseResponse}
     */
    @GetMapping("/api/users/info")
    BaseResponse<UserInfoResponseDto> sendUserInfoRequest(
            @RequestHeader("X-User-Id") Long userId); // RequestHeader로 유저 ID 넘겨주기

}
