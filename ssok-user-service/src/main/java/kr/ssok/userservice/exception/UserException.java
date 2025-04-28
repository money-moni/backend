package kr.ssok.userservice.exception;

import kr.ssok.common.exception.BaseException;
import kr.ssok.common.exception.ResponseStatus;
import lombok.Getter;

@Getter
public class UserException extends BaseException {
    public UserException(ResponseStatus status) {
        super(status);
    }
}
