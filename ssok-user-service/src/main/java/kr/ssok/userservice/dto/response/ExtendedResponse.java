package kr.ssok.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 확장된 응답 객체
 * 기본 BaseResponse를 확장하여 추가 정보를 포함할 수 있습니다.
 * 주로 로그인 실패, 계정 제한 등에 대한 상세 정보 제공에 사용됩니다.
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
public class ExtendedResponse<T> extends BaseResponse<T> {

    /**
     * 추가 정보 필드
     * 로그인 시도 횟수, 잠금 상태 해제 시간 등의 정보를 포함합니다.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> additionalInfo = new HashMap<>();

    /**
     * 상태와 결과로 응답 객체 생성
     * 
     * @param status 응답 상태
     * @param result 응답 결과 데이터
     */
    public ExtendedResponse(ResponseStatus status, T result) {
        super(status, result);
    }

    /**
     * 상태만으로 응답 객체 생성
     * 
     * @param status 응답 상태
     */
    public ExtendedResponse(ResponseStatus status) {
        super(status);
    }

    /**
     * 추가 정보 설정
     * 
     * @param additionalInfo 추가 정보 맵
     */
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        if (additionalInfo != null) {
            this.additionalInfo = additionalInfo;
        }
    }
    
    /**
     * 개별 추가 정보 항목 추가
     * 
     * @param key 정보 키
     * @param value 정보 값
     */
    public void addAdditionalInfo(String key, Object value) {
        this.additionalInfo.put(key, value);
    }
}
