package kr.ssok.transferservice.client;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.dto.response.OpenBankingResponse;
import kr.ssok.transferservice.config.FeignClientDecoderConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 오픈뱅킹 송금 요청을 보내는 Feign 클라이언트 인터페이스
 */
@FeignClient(name = "openbanking-service", url = "${external.openbanking-service.url}", configuration = FeignClientDecoderConfig.class)
public interface OpenBankingClient {

    /**
     * 오픈뱅킹 송금 요청 API 호출
     *
     * @param requestBody 요청 바디 (송금 정보)
     * @return OpenBankingResponse 형식의 송금 응답 객체
     */
    @PostMapping("/api/openbank/transfer")
    OpenBankingResponse sendTransferRequest(@RequestBody OpenBankingTransferRequestDto requestBody);
}
