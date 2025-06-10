package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.request.UpdateAliasRequestDto;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.service.AccountService;
import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.logging.annotation.ControllerLogging;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 계좌 관련 API 요청을 처리하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    /**
     * 계좌 생성 요청을 처리합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param createAccountRequestDto 클라이언트로부터 전달받은 계좌 생성 요청 데이터
     * @return 생성된 계좌 정보를 담은 {@link BaseResponse}
     */
    @PostMapping
    public ResponseEntity<BaseResponse<AccountResponseDto>> createAccount(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateAccountRequestDto createAccountRequestDto) {
        AccountResponseDto result = this.accountService.createLinkedAccount(Long.parseLong(userId), createAccountRequestDto);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_CREATE_SUCCESS, result));
    }

    /**
     * 사용자 ID에 해당하는 모든 연동 계좌 목록을 조회합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @return 조회된 연동 계좌 목록을 담은 {@link BaseResponse}
     */
    @GetMapping
    public CompletableFuture<ResponseEntity<BaseResponse<List<AccountBalanceResponseDto>>>> getAllAccounts(
            @RequestHeader("X-User-Id") String userId) {
        return accountService.findAllAccounts(Long.parseLong(userId))
                .thenApply(result ->
                        ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result))
                );
    }

    /**
     * 특정 계좌 ID에 해당하는 연동 계좌 상세 정보를 조회합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param accountId 조회할 계좌 ID (Path Variable)
     * @return 조회된 계좌 정보를 담은 {@link BaseResponse}
     */
    @GetMapping("/{accountId}")
    public CompletableFuture<ResponseEntity<BaseResponse<AccountBalanceResponseDto>>> getAccountById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("accountId") Long accountId) {
        return accountService.findAccountById(Long.parseLong(userId), accountId)
                .thenApply(result ->
                        ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result))
                );
    }

    /**
     * 특정 계좌 ID에 해당하는 연동 계좌를 삭제합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param accountId 삭제할 계좌 ID (Path Variable)
     * @return 삭제된 계좌 정보를 담은 {@link BaseResponse}
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<BaseResponse<AccountResponseDto>> deleteAccountById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("accountId") Long accountId) {
        AccountResponseDto result = this.accountService.deleteLinkedAccount(Long.parseLong(userId), accountId);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_DELETE_SUCCESS, result));
    }

    /**
     * 특정 계좌의 별명(alias)을 수정합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param accountId 별칭을 수정할 계좌 ID (Path Variable)
     * @return 수정된 계좌 정보를 담은 {@link BaseResponse}
     */
    @PatchMapping("/{accountId}/alias")
    public ResponseEntity<BaseResponse<AccountResponseDto>> updateAccountAlias(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("accountId") Long accountId,
            @RequestBody UpdateAliasRequestDto updateAliasRequestDto) {
        AccountResponseDto result = this.accountService.updateLinkedAccountAlias(Long.parseLong(userId), accountId, updateAliasRequestDto);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_ALIAS_UPDATE_SUCCESS, result));
    }

    /**
     * 특정 계좌를 주계좌(primary account)로 설정합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param accountId 주계좌로 지정할 계좌 ID (Path Variable)
     * @return 수정된 계좌 정보를 담은 {@link BaseResponse}
     */
    @PatchMapping("/{accountId}/primary")
    public ResponseEntity<BaseResponse<AccountResponseDto>> updatePrimaryAccount(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("accountId") Long accountId) {
        AccountResponseDto result = this.accountService.updatePrimaryLinkedAccount(Long.parseLong(userId), accountId);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_PRIMARY_UPDATE_SUCCESS, result));
    }


}
