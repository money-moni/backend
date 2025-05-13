package kr.ssok.transferservice.client.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 오픈뱅킹 전용 응답 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class OpenBankingResponse {

    @JsonProperty("isSuccess")
    private Boolean isSuccess;
    private String code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> result;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.isSuccess);
    }
}