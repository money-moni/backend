package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdsResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.PrimaryAccountInfoResponseDto;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.service.AccountInternalService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 서비스 간 연동을 위한 계좌 정보 API를 제공하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountInternalController {

    private final AccountInternalService accountInternalService;

    /**
     * 사용자 ID와 계좌 ID를 기반으로 해당 계좌의 상세 정보를 조회합니다.
     *
     * @param userId Gateway 또는 내부 요청 헤더로 전달된 사용자 ID (요청 헤더: X-User-Id)
     * @param accountId 조회할 계좌 ID (Query Parameter)
     * @return 사용자 ID, 계좌 ID, 계좌 번호를 포함한 계좌 상세 정보를 담은 {@link BaseResponse}
     */
    @GetMapping("/account-lookup")
    public ResponseEntity<BaseResponse<AccountInfoResponseDto>> getAccountInfo(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("accountId") Long accountId) {
        AccountInfoResponseDto result = this.accountInternalService.findAccountByUserIdAndAccountId(Long.parseLong(userId), accountId);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }

    /**
     * 계좌번호를 기반으로 계좌 ID, 유저 ID를 조회합니다.
     *
     * @param accountNumber 조회할 계좌 번호 (Query Parameter)
     * @return 해당 계좌 번호에 대한 계좌 ID, 유저 ID를 담은 {@link BaseResponse}
     */
    @GetMapping("/accounts/id")
    public ResponseEntity<BaseResponse<AccountIdResponseDto>> getAccountIdByAccountNumber(
            @RequestParam("accountNumber") String accountNumber) {
        AccountIdResponseDto result = this.accountInternalService.findAccountIdByAccountNumber(accountNumber);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }

    /**
     * 특정 사용자 ID에 해당하는 모든 계좌의 ID 목록을 조회합니다.
     *
     * @param userId Gateway 또는 내부 요청 헤더로 전달된 사용자 ID (요청 헤더: X-User-Id)
     * @return 해당 사용자의 모든 계좌 ID 목록을 담은 {@link BaseResponse}
     */
    @GetMapping("/accounts/ids")
    public ResponseEntity<BaseResponse<List<AccountIdsResponseDto>>> getAllAccountIds(
            @RequestHeader("X-User-Id") String userId) {
        List<AccountIdsResponseDto> result = this.accountInternalService.findAllAccountIds(Long.parseLong(userId));

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }

    /**
     * 사용자 ID를 기반으로 주계좌 정보를 조회합니다.
     *
     * @param userId Gateway 또는 내부 요청 헤더로 전달된 사용자 ID (요청 헤더: X-User-Id)
     * @return 사용자 ID에 해당하는 주계좌 정보를 담은 {@link BaseResponse}
     */
    @GetMapping("accounts/user-info")
    public ResponseEntity<BaseResponse<PrimaryAccountInfoResponseDto>> getPrimaryAccountInfo(
            @RequestHeader("X-User-Id") String userId) {
        PrimaryAccountInfoResponseDto result = this.accountInternalService.findPrimaryAccountByUserId(Long.parseLong(userId));

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }

}
