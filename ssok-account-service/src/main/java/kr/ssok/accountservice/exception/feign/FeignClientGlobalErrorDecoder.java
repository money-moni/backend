package kr.ssok.accountservice.exception.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Feign 클라이언트의 전역 예외 디코더.
 *
 * <p>Feign 통신 중 HTTP 4xx 또는 5xx 응답이 발생했을 때 해당 응답 바디를 파싱하여,
 * 비즈니스 응답 코드(`code`)에 따라 {@link AccountException}을 발생시킵니다.</p>
 */
@Slf4j
@Component
public class FeignClientGlobalErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Feign 응답을 해석하여 비즈니스 로직 기반의 예외를 반환합니다.
     *
     * @param methodKey 호출된 Feign 메서드 키 (클라이언트 메서드명)
     * @param response  Feign 응답 객체 (HTTP 응답)
     * @return 처리된 예외 객체 ({@link AccountException})
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 응답 바디를 InputStream으로 읽어서 문자열로 변환
            String responseBody = new BufferedReader(new InputStreamReader(
                    response.body().asInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            log.error("Feign Client Error - Method: {}, Status: {}, Response: {}",
                    methodKey, response.status(), responseBody);

            // ResponseBody 내 "code" 부분만 추출
            JsonNode root = objectMapper.readTree(responseBody);
            String errorCode = root.path("code").asText();  // "code" 필드가 없을 시, 빈 문자열 반환

            // 커스텀 코드 기반으로 예외 처리
            return switch (errorCode) {
                case "READ001" -> new AccountException(AccountResponseStatus.OPENBANKING_ACCOUNT_LIST_FAILED);
                case "READ002" -> new AccountException(AccountResponseStatus.OPENBANKING_BALANCE_LOOKUP_FAILED);
                case "READ003" -> new AccountException(AccountResponseStatus.OPENBANKING_OWNER_LOOKUP_FAILED);
                default -> new AccountException(AccountResponseStatus.INVALID_OPENBANKING_CODE);
            };
        } catch (IOException | ClassCastException | NullPointerException e) {
            log.error("Error while decoding Feign response: ", e);
            return new AccountException(AccountResponseStatus.OPENBANKING_REQUEST_FAILED);
        }
    }
}
