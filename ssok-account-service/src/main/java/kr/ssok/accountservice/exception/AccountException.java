package kr.ssok.accountservice.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;

/**
 * 계좌 서비스 전용 예외 클래스
 */
public class AccountException extends BaseException {
    public AccountException(ResponseStatus status) {
        super(status);
    }
}
