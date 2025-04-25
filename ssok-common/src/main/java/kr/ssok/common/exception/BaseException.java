package kr.ssok.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
