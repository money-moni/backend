package kr.ssok.accountservice.client;

import kr.ssok.accountservice.client.dto.response.OpenBankingResponse;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 오픈뱅킹 서버와의 통신을 위한 WebClient 기반 클라이언트 컴포넌트
 *
 * <p>오픈뱅킹 서버로 계좌 목록, 잔액, 실명 확인 요청을 전달합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class OpenBankingClient {
    private final WebClient openBankingWebClient;

    @Value("${external.openbanking-service.api-key}")
    private String openBankingApiKey;

    /**
     * 사용자의 전체 계좌 목록을 비동기로 조회합니다.
     *
     * @param dto 오픈뱅킹 전체 계좌 조회 요청에 필요한 사용자 정보
     * @return 오픈뱅킹 서버로부터 전달받은 전체 계좌 목록 응답을 포함하는 {@link CompletableFuture}
     */
    public CompletableFuture<OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>>> sendAllAccountsRequest(OpenBankingAllAccountsRequestDto dto) {
        return openBankingWebClient.post()
                .uri("/api/openbank/accounts/request")
                .header("X-API-KEY", openBankingApiKey)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>>>() {})
                .toFuture();
    }

    /**
     * 특정 계좌에 대한 실명 정보를 비동기로 조회합니다.
     *
     * @param dto 실명 확인 요청에 필요한 계좌 번호 및 은행 코드 정보
     * @return 예금주 실명 정보가 포함된 {@link CompletableFuture}
     */
    public CompletableFuture<OpenBankingResponse<OpenBankingAccountOwnerResponseDto>> sendAccountOwnerRequest(OpenBankingAccountOwnerRequestDto dto) {
        return openBankingWebClient.post()
                .uri("/api/openbank/account/verify-name")
                .header("X-API-KEY", openBankingApiKey)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OpenBankingResponse<OpenBankingAccountOwnerResponseDto>>() {})
                .toFuture();
    }

    /**
     * 특정 계좌의 잔액 정보를 비동기로 조회합니다.
     *
     * @param dto 계좌 번호 및 은행 코드가 포함된 잔액 조회 요청 DTO
     * @return 계좌 잔액 정보가 포함된 {@link CompletableFuture}
     */
    public CompletableFuture<OpenBankingResponse<OpenBankingAccountBalanceResponseDto>> sendAccountBalanceRequest(OpenBankingAccountBalanceRequestDto dto) {
        return openBankingWebClient.post()
                .uri("/api/openbank/account/balance")
                .header("X-API-KEY", openBankingApiKey)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OpenBankingResponse<OpenBankingAccountBalanceResponseDto>>() {})
                .toFuture();
    }

}
