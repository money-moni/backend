package kr.ssok.userservice.service;

import kr.ssok.userservice.dto.request.LoginRequestDto;
import kr.ssok.userservice.dto.response.LoginResponseDto;

/**
 * 인증 관련 서비스 인터페이스
 * 로그인, 로그아웃, 토큰 갱신 등의 인증 관련 비즈니스 로직을 정의합니다.
 */
public interface AuthService {

    /**
     * 로그인 처리
     * 사용자 ID와 PIN 코드를 검증하고 JWT 토큰을 발급합니다.
     * 
     * @param requestDto 로그인 요청 정보 (userId, pinCode)
     * @return 액세스 토큰과 리프레시 토큰이 포함된 응답
     */
    LoginResponseDto login(LoginRequestDto requestDto);

    /**
     * 토큰 갱신
     * Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 리프레시 토큰이 포함된 응답
     */
    LoginResponseDto refreshToken(String refreshToken);

    /**
     * 로그아웃 처리
     * Access Token을 블랙리스트에 추가하고, Refresh Token을 삭제합니다.
     * 
     * @param accessToken 로그아웃할 액세스 토큰
     */
    void logout(String accessToken);

    /**
     * 앱 백그라운드 전환 처리
     * 현재 Access Token을 블랙리스트에 추가합니다.
     * 
     * @param accessToken 현재 사용 중인 액세스 토큰
     */
    void handleBackground(String accessToken);
    
    /**
     * 사용자 ID로 사용자 조회 (내부 인증용)
     * Gateway에서 인증 시 사용할 내부 메소드입니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     */
    Object getUserForAuth(Long userId);
}
