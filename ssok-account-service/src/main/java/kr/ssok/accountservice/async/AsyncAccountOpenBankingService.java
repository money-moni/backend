package kr.ssok.accountservice.async;

import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncAccountOpenBankingService {
    private final AccountOpenBankingService accountOpenBankingService;

    @Async("customExecutor")
    public CompletableFuture<List<AllAccountsResponseDto>> fetchAllAccountsAsync(Long userId) {
        List<AllAccountsResponseDto> result = accountOpenBankingService.fetchAllAccountsFromOpenBanking(userId);
        return CompletableFuture.completedFuture(result);
    }

    @Async("customExecutor")
    public CompletableFuture<AccountOwnerResponseDto> fetchAccountOwnerAsync(AccountOwnerRequestDto dto) {
        AccountOwnerResponseDto result = accountOpenBankingService.fetchAccountOwnerFromOpenBanking(dto);
        return CompletableFuture.completedFuture(result);
    }
}
