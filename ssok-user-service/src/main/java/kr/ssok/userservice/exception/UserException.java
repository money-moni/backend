package kr.ssok.userservice.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 예외 클래스
 * 사용자 서비스에서 발생하는 예외를 처리하며, 추가 정보를 포함할 수 있습니다.
 */
@Getter
public class UserException extends BaseException {
    
    /**
     * 클라이언트에게 전달할 추가 정보를 담는 맵
     */
    private final Map<String, Object> additionalInfo = new HashMap<>();
    
    /**
     * 기본 생성자
     * 
     * @param status 응답 상태 정보
     */
    public UserException(ResponseStatus status) {
        super(status);
    }
    
    /**
     * 추가 정보를 포함하는 생성자
     * 
     * @param status 응답 상태 정보
     * @param additionalInfo 추가 정보 맵
     */
    public UserException(ResponseStatus status, Map<String, Object> additionalInfo) {
        super(status);
        if (additionalInfo != null) {
            this.additionalInfo.putAll(additionalInfo);
        }
    }
}
