package kr.ssok.transferservice.client.webclient;

import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.dto.response.OpenBankingResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * OpenBanking API 송금 호출을 위한 인터페이스
 * - WebClient 또는 Feign 등 다양한 구현체로 대체 가능
 */
public interface OpenBankingApiClient {

    /**
     * 송금 요청을 논-블로킹 방식으로 전송
     *
     * @param requestDto 송금에 필요한 정보
     * @return Mono<OpenBankingResponse> reactive 응답 스트림
     */
    Mono<OpenBankingResponse> sendTransferRequest(OpenBankingTransferRequestDto requestDto);

    /**
     * CompletableFuture 형태로 송금 요청 전송
     *
     * @param requestDto 송금에 필요한 정보
     * @return CompletableFuture<OpenBankingResponse>
     */
    CompletableFuture<OpenBankingResponse> sendTransferRequestAsync(OpenBankingTransferRequestDto requestDto);
}