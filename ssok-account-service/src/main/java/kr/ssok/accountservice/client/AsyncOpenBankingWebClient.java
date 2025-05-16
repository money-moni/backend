package kr.ssok.accountservice.client;

import kr.ssok.accountservice.client.dto.response.OpenBankingResponse;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class AsyncOpenBankingWebClient {
    private final WebClient openBankingWebClient;

    @Value("${external.openbanking-service.api-key}")
    private String apiKey;

    public CompletableFuture<OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>>> sendAllAccountsRequest(OpenBankingAllAccountsRequestDto dto) {
        return openBankingWebClient.post()
                .uri("/api/openbank/accounts/request")
                .header("X-API-KEY", apiKey)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>>>() {})
                .toFuture();
    }

    public CompletableFuture<OpenBankingResponse<OpenBankingAccountOwnerResponseDto>> sendAccountOwnerRequest(OpenBankingAccountOwnerRequestDto dto) {
        return openBankingWebClient.post()
                .uri("/api/openbank/account/verify-name")
                .header("X-API-KEY", apiKey)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OpenBankingResponse<OpenBankingAccountOwnerResponseDto>>() {})
                .toFuture();
    }
}
