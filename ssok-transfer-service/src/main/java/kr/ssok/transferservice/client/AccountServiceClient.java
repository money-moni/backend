package kr.ssok.transferservice.client;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.response.AccountIdsResponseDto;
import kr.ssok.transferservice.client.dto.response.AccountResponseDto;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 계좌 서비스와 통신하는 Feign 클라이언트 인터페이스
 * feign 통신의 에러 처리는 이후에 고려
 */
@FeignClient(name = "account-service", url = "${external.account-service.url}") // name 임시 지정, url은 실행을 위해 임시로 지정
public interface AccountServiceClient {

    /**
     * 계좌 ID와 사용자 ID를 기반으로 계좌번호를 조회
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return BaseResponse 형식의 계좌 응답 객체 (계좌번호)
     */
    @GetMapping("/api/account-lookup")
    BaseResponse<AccountResponseDto> getAccountInfo(
            @RequestParam("accountId") Long accountId,
            @RequestHeader("X-User-Id") String userId
    );

    /**
     * 계좌번호를 기반으로 계좌 ID를 조회
     *
     * @param accountNumber 조회할 계좌번호
     * @return BaseResponse 형식의 계좌 응답 객체 (계좌 ID)
     * */
    @GetMapping("/api/accounts/id")
    BaseResponse<AccountIdResponseDto> getAccountId(
            @RequestParam("accountNumber") String accountNumber
    );

    /**
     * 사용자 ID를 기반으로 해당 사용자의 모든 계좌 ID 리스트를 조회
     *
     * @param userId 사용자 ID
     * @return BaseResponse 객체에 계좌 ID 리스트를 담아 반환
     */
    @GetMapping("/api/accounts/ids")
    BaseResponse<List<AccountIdsResponseDto>> getAccountIdsByUserId(
            @RequestHeader("X-User-Id") String userId);

    /**
     * 사용자 주 계좌 정보를 조회
     *
     * @param userId 사용자 ID를 담고 있는 헤더 값 (X-User-Id)
     * @return BaseResponse<AccountResponseDto> 사용자 계좌 정보가 포함된 응답
     */
    @GetMapping("/api/accounts/user-info")
    BaseResponse<PrimaryAccountResponseDto> getAccountInfo(
            @RequestHeader("X-User-Id") String userId);
}
