package kr.ssok.userservice.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;

/**
 * 사용자 서비스 전용 예외 클래스
 */
public class UserException extends BaseException {
    public UserException(ResponseStatus status) {
        super(status);
    }
}
