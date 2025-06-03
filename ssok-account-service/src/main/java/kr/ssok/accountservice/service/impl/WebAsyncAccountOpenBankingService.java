package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.client.OpenBankingClient;
import kr.ssok.accountservice.client.UserServiceClient;
import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.util.AccountIdentifierUtil;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebAsyncAccountOpenBankingService {
    private final UserServiceClient userServiceClient;
    private final OpenBankingClient openBankingClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Async("customExecutorWebClient")
    public CompletableFuture<List<AllAccountsResponseDto>> fetchAllAccountsAsync(Long userId) {
        BaseResponse<UserInfoResponseDto> userInfoResponse = userServiceClient.sendUserInfoRequest(userId.toString());

        if (userInfoResponse == null || userInfoResponse.getResult() == null) {
            log.warn("[USERSERVICE] 사용자 정보 조회 실패: userId={}", userId);
            throw new AccountException(AccountResponseStatus.USER_INFO_NOT_FOUND);
        }

        OpenBankingAllAccountsRequestDto requestDto = OpenBankingAllAccountsRequestDto.from(userInfoResponse.getResult());

        return openBankingClient.sendAllAccountsRequest(requestDto)
                .thenApply(response -> {
                    if (response == null || response.getResult() == null) {
                        log.warn("[OPENBANKING] 전체 계좌 조회 실패: userId={}, username={}", userId, requestDto.getUsername());
                        throw new AccountException(AccountResponseStatus.OPENBANKING_ACCOUNT_LIST_FAILED);
                    }

                    cacheAvailableAccountSet(userId, response.getResult());

                    return response.getResult().stream()
                            .map(AllAccountsResponseDto::from)
                            .toList();
                })
                .exceptionally(e -> {
                    log.error("[OPENBANKING] 오픈뱅킹 전체 계좌 조회 비동기 에러", e);
                    throw new CompletionException(e);
                });
    }

    @Async("customExecutorWebClient")
    public CompletableFuture<AccountOwnerResponseDto> fetchAccountOwnerAsync(AccountOwnerRequestDto dto) {
        OpenBankingAccountOwnerRequestDto requestDto = OpenBankingAccountOwnerRequestDto.from(dto);

        return openBankingClient.sendAccountOwnerRequest(requestDto)
                .thenApply(response -> {
                    if (response == null || response.getResult() == null) {
                        log.warn("[OPENBANKING] 실명 조회 실패: accountNumber={}, bankCode={}", requestDto.getAccountNumber(), requestDto.getBankCode());
                        throw new AccountException(AccountResponseStatus.OPENBANKING_OWNER_LOOKUP_FAILED);
                    }

                    return AccountOwnerResponseDto.from(response.getResult(), dto.getAccountNumber());
                })
                .exceptionally(e -> {
                    log.error("[OPENBANKING] 오픈뱅킹 실명 조회 비동기 에러", e);
                    throw new CompletionException(e);
                });
    }

    @Async("customExecutorWebClient")
    public CompletableFuture<OpenBankingAccountBalanceResponseDto> fetchAccountBalanceAsync(
            OpenBankingAccountBalanceRequestDto request) {
        return openBankingClient.sendAccountBalanceRequest(request)
                .thenApplyAsync(response -> {
                    if (response == null || response.getResult() == null) {
                        log.error("[OPENBANKING] 잔액 조회 실패: bankCode={}, accountNumber={}", request.getBankCode(), request.getAccountNumber());
                        throw new AccountException(AccountResponseStatus.OPENBANKING_BALANCE_LOOKUP_FAILED);
                    }
                    return response.getResult();
                })
                .exceptionally(e -> {
                    log.error("[OPENBANKING] 오픈뱅킹 잔액 조회 비동기 에러", e);
                    throw new CompletionException(e);
                });
    }

    private void cacheAvailableAccountSet(Long userId, List<OpenBankingAllAccountsResponseDto> accounts) {
        String redisKey = AccountIdentifierUtil.buildLookupKey(userId);

        redisTemplate.opsForSet().add(redisKey,
                accounts.stream()
                        .map(dto -> AccountIdentifierUtil.buildLookupValue(
                                dto.getBankCode(),
                                dto.getAccountNumber(),
                                dto.getAccountTypeCode()))
                        .distinct()
                        .toArray(String[]::new));

        redisTemplate.expire(redisKey, Duration.ofMinutes(5)); // TTL 5분
    }
}
