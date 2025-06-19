package kr.ssok.transferservice.client.webclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.dto.response.OpenBankingResponse;
import kr.ssok.transferservice.client.webclient.OpenBankingApiClient;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * 외부 OpenBanking API 송금 호출을 전담하는 컴포넌트
 * - WebClient 논-블로킹 I/O를 직접 사용
 * - Mono<OpenBankingResponse> 또는 CompletableFuture 형태로 결과를 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenBankingApiClientImpl implements OpenBankingApiClient {

    /** 오픈뱅킹 API 키 */
    @Value("${external.openbanking-service.api-key}")
    private String apiKey;

    /** 외부 호출용 WebClient 빈 (baseUrl 은 여기서 지정하지 않아도 됨) */
    private final WebClient openBankingWebClient;

    private final ObjectMapper objectMapper;

    /**
     * 논-블로킹 방식으로 송금 요청을 보냄
     *
     * @param requestDto 송금에 필요한 모든 정보(DTO)
     * @return Mono<OpenBankingResponse> - Reactive 스트림으로 성공/실패 응답
     */
    @Override
    public Mono<OpenBankingResponse> sendTransferRequest(OpenBankingTransferRequestDto requestDto) {
        return openBankingWebClient
                .post()
                .uri("/api/openbank/transfers")
                .header("X-API-KEY", apiKey)     // API 키 헤더
                .bodyValue(requestDto)                      // 요청 바디
                .retrieve()                                 // HTTP 호출
                // HTTP 4xx 에러 처리 & HTTP 5xx 에러 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            try {
                                OpenBankingResponse err = objectMapper.readValue(body, OpenBankingResponse.class);
                                return Mono.error(mapToTransferException(err));
                            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                log.error("오픈 뱅킹 WebClient Error parsing error response", e);
                                return Mono.error(new TransferException(TransferResponseStatus.JSON_PARSE_ERROR));
                            }
                        })
                )
                // 정상 응답도 isSuccess=false 면 예외
                .bodyToMono(OpenBankingResponse.class)
                .flatMap(resp -> {
                    if (!resp.isSuccess()) {
                        return Mono.error(mapToTransferException(resp));
                    }
                    return Mono.just(resp);
                });
    }

    /**
     * CompletableFuture 형태로도 사용할 수 있도록 변환해 줌
     *
     * @param requestDto 송금 요청 DTO
     * @return CompletableFuture<OpenBankingResponse>
     */
    @Override
    public CompletableFuture<OpenBankingResponse> sendTransferRequestAsync(OpenBankingTransferRequestDto requestDto) {
        return sendTransferRequest(requestDto).toFuture();
    }

    /** OpenBankingResponse.code 에 따라 TransferException 으로 변환 */
    private TransferException mapToTransferException(OpenBankingResponse resp) {
        String code = resp.getCode();
        return switch (code) {
            case "ACCOUNT001" -> new TransferException(TransferResponseStatus.ACCOUNT_NOT_FOUND);
            case "ACCOUNT002" -> new TransferException(TransferResponseStatus.DORMANT_ACCOUNT);
            case "READ001"    -> new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
            case "TRANSFER004"-> new TransferException(TransferResponseStatus.INSUFFICIENT_BALANCE);
            case "TRANSFER005"-> new TransferException(TransferResponseStatus.TRANSFER_LIMIT_EXCEEDED);
            case "TRANSFER006"-> new TransferException(TransferResponseStatus.TRANSFER_FAILED);
            case "TRANSFER002"-> new TransferException(TransferResponseStatus.WITHDRAWAL_ERROR);
            case "TRANSFER003"-> new TransferException(TransferResponseStatus.DEPOSIT_ERROR);
            case "BAD_GATEWAY"-> new TransferException(TransferResponseStatus.BANK_API_COMMUNICATION_FAILED);
            case "COMMON500"  -> new TransferException(TransferResponseStatus.TRANSFER_UNKNOWN_ERROR);
            default           -> new TransferException(TransferResponseStatus.TRANSFER_FAILED);
        };
    }
}