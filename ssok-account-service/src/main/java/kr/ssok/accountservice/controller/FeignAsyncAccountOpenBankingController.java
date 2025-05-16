package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.async.FeignAsyncAccountOpenBankingService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 오픈뱅킹 관련 API 요청을 처리하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/accounts/openbank/async/feign")
@RequiredArgsConstructor
public class FeignAsyncAccountOpenBankingController {
    private final FeignAsyncAccountOpenBankingService feignAsyncAccountOpenBankingService;

    @PostMapping
    public CompletableFuture<ResponseEntity<BaseResponse<List<AllAccountsResponseDto>>>> getOpenBankingAccounts(
            @RequestHeader("X-User-Id") String userId) {

        return this.feignAsyncAccountOpenBankingService.fetchAllAccountsAsync(Long.parseLong(userId))
                .thenApply(result -> ResponseEntity.ok(
                        new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result)
                ));
    }

    @PostMapping("/verify-name")
    public CompletableFuture<ResponseEntity<BaseResponse<AccountOwnerResponseDto>>> getOpenBankingAccountOwner(
            @RequestBody AccountOwnerRequestDto accountOwnerRequestDto) {

        return feignAsyncAccountOpenBankingService.fetchAccountOwnerAsync(accountOwnerRequestDto)
                .thenApply(result ->
                        ResponseEntity.ok(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result))
                );
    }
}
