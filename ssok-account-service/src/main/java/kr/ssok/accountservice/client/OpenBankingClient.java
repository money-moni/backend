package kr.ssok.accountservice.client;

import kr.ssok.accountservice.client.dto.response.OpenBankingResponse;
import kr.ssok.accountservice.config.FeignClientDecoderConfig;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 오픈뱅킹 서버와의 통신을 위한 Feign Client 인터페이스
 *
 * <p>오픈뱅킹 서버로 계좌 목록, 잔액, 실명 확인 요청을 전달합니다.</p>
 */
@FeignClient(name = "openbanking-service", url = "${external.openbanking-service.url}", configuration = FeignClientDecoderConfig.class)
public interface OpenBankingClient {

    /**
     * 사용자의 전체 계좌 목록을 조회합니다.
     *
     * @param requestBody 오픈뱅킹 전체 계좌 조회 요청에 필요한 사용자 정보
     * @return 오픈뱅킹 서버로부터 전달받은 전체 계좌 목록 응답 {@link OpenBankingResponse}
     */
    @PostMapping("/api/openbank/accounts/request")
    OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>> sendAllAccountsRequest(
            @RequestHeader("X-API-KEY") String openBankingApiKey,
            @RequestBody OpenBankingAllAccountsRequestDto requestBody);

    /**
     * 특정 계좌의 잔액을 조회합니다.
     *
     * @param requestBody 계좌 번호 및 은행 코드가 포함된 요청 DTO
     * @return 계좌 잔액 정보가 포함된 {@link OpenBankingResponse}
     */
    @GetMapping("/api/openbank/account/balance")
    OpenBankingResponse<OpenBankingAccountBalanceResponseDto> sendAccountBalanceRequest(
            @RequestHeader("X-API-KEY") String openBankingApiKey,
            @RequestBody OpenBankingAccountBalanceRequestDto requestBody);

    /**
     * 특정 계좌에 대한 실명 정보를 조회합니다.
     *
     * @param requestBody 계좌 번호 및 은행 코드가 포함된 요청 DTO
     * @return 예금주 실명 정보가 포함된 {@link OpenBankingResponse}
     */
    @PostMapping("/api/openbank/account/verify-name")
    OpenBankingResponse<OpenBankingAccountOwnerResponseDto> sendAccountOwnerRequest(
            @RequestHeader("X-API-KEY") String openBankingApiKey,
            @RequestBody OpenBankingAccountOwnerRequestDto requestBody);
}
