package kr.ssok.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 응답 상태 코드를 정의하는 인터페이스
 * 각 도메인별 모듈에서 이 인터페이스를 구현한 Enum을 정의하여 사용
 */
public interface ResponseStatus {
    boolean isSuccess();
    int getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
