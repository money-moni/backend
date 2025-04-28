package kr.ssok.transferservice.client;

import kr.ssok.common.exception.BaseResponse;
import lombok.Getter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    BaseResponse<AccountResponse.Result> getAccountInfo(
            @RequestParam("accountId") Long accountId,
            @RequestParam("userId") Long userId
    );

    /**
     * 계좌번호를 기반으로 계좌 ID를 조회
     *
     * @param accountNumber 조회할 계좌번호
     * @return BaseResponse 형식의 계좌 응답 객체 (계좌 ID)
     * */
    @GetMapping("/api/accounts/id")
    BaseResponse<AccountIdResponse.Result> getAccountId(
            @RequestParam("accountNumber") String accountNumber
    );

    /**
     * 계좌 번호 응답 객체 구조
     */
    @Getter
    class AccountResponse {
        @Getter
        public static class Result {
            private final String accountNumber;

            public Result(String accountNumber) {
                this.accountNumber = accountNumber;
            }
        }
    }

    /**
     * 계좌 ID 응답 객체 구조
     */
    @Getter
    class AccountIdResponse {
        @Getter
        public static class Result {
            private final Long accountId;

            public Result(Long accountId) {
                this.accountId = accountId;
            }
        }
    }
}
