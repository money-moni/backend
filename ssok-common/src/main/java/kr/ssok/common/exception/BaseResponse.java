package kr.ssok.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 응답 객체
 * 모든 API 응답은 이 클래스를 통해 일관된 형식으로 반환
 * @param <T> 응답 데이터 타입
 */
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final int code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    public BaseResponse(ResponseStatus status, String message, T result) {
        this.isSuccess = status.isSuccess();
        this.message = message;
        this.code = status.getCode();
        this.result = result;
    }

    public BaseResponse(ResponseStatus status, T result) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = result;
    }

    public BaseResponse(ResponseStatus status) {
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = null;
    }
}
