package kr.ssok.userservice.controller;

import jakarta.validation.Valid;
import kr.ssok.common.exception.BaseResponse;
import kr.ssok.userservice.dto.request.LoginRequestDto;
import kr.ssok.userservice.dto.request.TokenRefreshRequestDto;
import kr.ssok.userservice.dto.response.LoginResponseDto;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API를 처리하는 컨트롤러
 * 로그인, 로그아웃, 토큰 갱신 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 API
     * 사용자 ID와 PIN 코드로 인증하고 JWT 토큰을 발급합니다.
     * 
     * @param requestDto 로그인 요청 정보 (userId, pinCode)
     * @return 액세스 토큰과 리프레시 토큰이 포함된 응답
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청. 사용자 ID: {}", requestDto.getUserId());
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.LOGIN_SUCCESS, responseDto));
    }

    /**
     * 토큰 갱신 API
     * Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
     * 
     * @param requestDto 토큰 갱신 요청 정보 (refreshToken)
     * @return 새로운 액세스 토큰과 리프레시 토큰이 포함된 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<LoginResponseDto>> refreshToken(@Valid @RequestBody TokenRefreshRequestDto requestDto) {
        log.info("토큰 갱신 요청");
        LoginResponseDto responseDto = authService.refreshToken(requestDto.getRefreshToken());
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.TOKEN_REFRESH_SUCCESS, responseDto));
    }

    /**
     * 로그아웃 API
     * 현재 Access Token을 블랙리스트에 추가하고 Refresh Token을 삭제합니다.
     * 
     * @param authorization Authorization 헤더 (Bearer 토큰)
     * @return 로그아웃 성공 여부
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        log.info("로그아웃 요청");
        authService.logout(authorization);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.LOGOUT_SUCCESS));
    }

    /**
     * 앱 백그라운드 전환 API
     * 현재 Access Token을 블랙리스트에 추가하여 보안을 강화합니다.
     * 
     * @param authorization Authorization 헤더 (Bearer 토큰)
     * @return 처리 결과
     */
    @PostMapping("/background")
    public ResponseEntity<BaseResponse<Void>> handleBackground(@RequestHeader("Authorization") String authorization) {
        log.info("백그라운드 전환 요청");
        authService.handleBackground(authorization);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    /**
     * 앱 포그라운드 복귀 API
     * 로그인 재인증을 통해 새로운 토큰을 발급받습니다.
     * 
     * @param requestDto 로그인 요청 정보 (userId, pinCode)
     * @return 새로운 액세스 토큰과 리프레시 토큰이 포함된 응답
     */
    @PostMapping("/foreground")
    public ResponseEntity<BaseResponse<LoginResponseDto>> handleForeground(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("포그라운드 복귀 요청. 사용자 ID: {}", requestDto.getUserId());
        // 포그라운드 복귀는 실제로 새로운 로그인 요청과 동일하게 처리
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.LOGIN_SUCCESS, responseDto));
    }

    /**
     * 사용자 인증 정보 조회 API (내부용)
     * 게이트웨이에서 인증 시 사용할 내부 API입니다.
     * 
     * @param userId 조회할 사용자 ID
     * @return 인증에 필요한 사용자 정보
     */
    @GetMapping("/internal/{userId}")
    public ResponseEntity<User> getUserForAuth(@PathVariable Long userId) {
        log.info("내부 인증용 사용자 정보 조회. 사용자 ID: {}", userId);
        User user = (User) authService.getUserForAuth(userId);
        return ResponseEntity.ok(user);
    }
}
