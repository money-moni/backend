package kr.ssok.common.exception;

import lombok.Getter;

/**
 * 공통 예외 클래스
 * 모든 도메인별 예외는 이 클래스를 상속받아 구현
 */
@Getter
public class BaseException extends RuntimeException {

    private ResponseStatus status;

    public BaseException(ResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
