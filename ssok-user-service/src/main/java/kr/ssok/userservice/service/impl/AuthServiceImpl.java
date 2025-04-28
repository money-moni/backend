package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.dto.request.LoginRequestDto;
import kr.ssok.userservice.dto.response.LoginResponseDto;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.security.jwt.JwtTokenProvider;
import kr.ssok.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 인증 서비스 구현체
 * 사용자 인증, 토큰 관리, 로그인 시도 제한 등의 기능을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    
    // 로그인 시도 횟수를 저장하는 키 prefix
    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    // 로그인 제한 상태를 저장하는 키 prefix
    private static final String LOGIN_BLOCKED_PREFIX = "login:blocked:";
    // 계정 잠금 상태를 저장하는 키 prefix
    private static final String ACCOUNT_LOCKED_PREFIX = "account:locked:";
    // Refresh Token을 저장하는 키 prefix
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
    // 블랙리스트 토큰을 저장하는 키 prefix
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist:token:";
    
    // 로그인 시도 제한 횟수
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    // 로그인 제한 시간 (5분)
    private static final long LOGIN_BLOCKED_DURATION = 5 * 60;
    // 계정 잠금 시간 (24시간)
    private static final long ACCOUNT_LOCKED_DURATION = 24 * 60 * 60;
    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 로그인 처리 구현
     * 사용자 ID와 PIN 코드 검증 후 토큰 발급, 로그인 시도 제한 관리
     * 실패 시 남은 시도 횟수, 제한 해제 시간 등 추가 정보 제공
     * 
     * @param requestDto 로그인 요청 정보 (userId, pinCode)
     * @return 액세스 토큰과 리프레시 토큰이 포함된 응답
     * @throws UserException 계정 잠금, 로그인 제한, 인증 실패 등의 경우 발생
     */
    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // 계정 잠금 확인
        String accountLockedKey = ACCOUNT_LOCKED_PREFIX + requestDto.getUserId();
        Boolean isAccountLocked = redisTemplate.hasKey(accountLockedKey);
        if (Boolean.TRUE.equals(isAccountLocked)) {
            Map<String, Object> additionalInfo = new HashMap<>();
            
            // 잠금 해제 남은 시간 계산
            Long remainingSeconds = redisTemplate.getExpire(accountLockedKey, TimeUnit.SECONDS);
            if (remainingSeconds != null && remainingSeconds > 0) {
                additionalInfo.put("remainingSeconds", remainingSeconds);
                additionalInfo.put("remainingHours", Math.ceil(remainingSeconds / 3600.0));
                
                // 잠금 해제 예상 시간 추가
                LocalDateTime unlockTime = LocalDateTime.now().plusSeconds(remainingSeconds);
                additionalInfo.put("unlockTime", unlockTime.format(DATE_FORMATTER));
            }
            
            throw new UserException(UserResponseStatus.ACCOUNT_LOCKED, additionalInfo);
        }
        
        // 로그인 제한 확인
        String loginBlockedKey = LOGIN_BLOCKED_PREFIX + requestDto.getUserId();
        Boolean isLoginBlocked = redisTemplate.hasKey(loginBlockedKey);
        if (Boolean.TRUE.equals(isLoginBlocked)) {
            Map<String, Object> additionalInfo = new HashMap<>();
            
            // 제한 해제 남은 시간 계산
            Long remainingSeconds = redisTemplate.getExpire(loginBlockedKey, TimeUnit.SECONDS);
            if (remainingSeconds != null && remainingSeconds > 0) {
                additionalInfo.put("remainingSeconds", remainingSeconds);
                additionalInfo.put("remainingMinutes", Math.ceil(remainingSeconds / 60.0));
                
                // 제한 해제 예상 시간 추가
                LocalDateTime unlockTime = LocalDateTime.now().plusSeconds(remainingSeconds);
                additionalInfo.put("unlockTime", unlockTime.format(DATE_FORMATTER));
            }
            
            throw new UserException(UserResponseStatus.TOO_MANY_LOGIN_ATTEMPTS, additionalInfo);
        }
        
        // 사용자 정보 조회
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new UserException(UserResponseStatus.USER_NOT_FOUND));
        
        // PIN 코드 검증
        if (!passwordEncoder.matches(String.valueOf(requestDto.getPinCode()), user.getPinCode())) {
            // 로그인 실패 시 시도 횟수 증가
            String loginAttemptKey = LOGIN_ATTEMPT_PREFIX + requestDto.getUserId();
            
            Long attempts = redisTemplate.opsForValue().increment(loginAttemptKey);
            if (attempts == 1) {
                // 첫 실패 시 key 만료 시간 설정 (24시간)
                redisTemplate.expire(loginAttemptKey, 24, TimeUnit.HOURS);
            }
            
            // 남은 시도 횟수 계산
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts.intValue();
            log.warn("로그인 실패. 사용자: {}, 시도 횟수: {}/{}, 남은 시도: {}", 
                    requestDto.getUserId(), attempts, MAX_LOGIN_ATTEMPTS, remainingAttempts);
            
            // 로그인 시도 횟수 초과 시 제한
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                // 이전 시도 횟수 초기화
                redisTemplate.delete(loginAttemptKey);
                
                // 로그인 제한 상태 저장
                redisTemplate.opsForValue().set(loginBlockedKey, "blocked", LOGIN_BLOCKED_DURATION, TimeUnit.SECONDS);
                
                // 로그인 제한 횟수 증가
                String loginBlockCountKey = LOGIN_BLOCKED_PREFIX + "count:" + requestDto.getUserId();
                Long blockCount = redisTemplate.opsForValue().increment(loginBlockCountKey);
                if (blockCount == 1) {
                    // 카운터 만료 시간 설정 (7일)
                    redisTemplate.expire(loginBlockCountKey, 7, TimeUnit.DAYS);
                }
                
                // 로그인 제한 정보 생성
                Map<String, Object> additionalInfo = new HashMap<>();
                additionalInfo.put("blockedDurationSeconds", LOGIN_BLOCKED_DURATION);
                additionalInfo.put("blockedDurationMinutes", LOGIN_BLOCKED_DURATION / 60);
                
                // 제한 해제 예상 시간 추가
                LocalDateTime unlockTime = LocalDateTime.now().plusSeconds(LOGIN_BLOCKED_DURATION);
                additionalInfo.put("unlockTime", unlockTime.format(DATE_FORMATTER));
                
                // 로그인 제한이 3회 이상이면 계정 잠금
                if (blockCount >= 3) {
                    redisTemplate.opsForValue().set(accountLockedKey, "locked", ACCOUNT_LOCKED_DURATION, TimeUnit.SECONDS);
                    
                    // 계정 잠금 정보 생성
                    additionalInfo.put("lockedDurationSeconds", ACCOUNT_LOCKED_DURATION);
                    additionalInfo.put("lockedDurationHours", ACCOUNT_LOCKED_DURATION / 3600);
                    
                    // 잠금 해제 예상 시간 추가
                    LocalDateTime lockUnlockTime = LocalDateTime.now().plusSeconds(ACCOUNT_LOCKED_DURATION);
                    additionalInfo.put("accountUnlockTime", lockUnlockTime.format(DATE_FORMATTER));
                    
                    throw new UserException(UserResponseStatus.ACCOUNT_LOCKED, additionalInfo);
                }
                
                throw new UserException(UserResponseStatus.TOO_MANY_LOGIN_ATTEMPTS, additionalInfo);
            }
            
            // 일반 PIN 불일치 오류 응답에 남은 시도 횟수 추가
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("remainingAttempts", remainingAttempts);
            throw new UserException(UserResponseStatus.INVALID_PIN_CODE, additionalInfo);
        }
        
        // 로그인 성공 시 시도 횟수 초기화
        redisTemplate.delete(LOGIN_ATTEMPT_PREFIX + requestDto.getUserId());
        
        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        
        // Refresh Token을 Redis에 저장
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(
                refreshTokenKey, 
                refreshToken, 
                jwtTokenProvider.getTokenExpirationTime(refreshToken), 
                TimeUnit.SECONDS
        );
        
        // 응답 생성
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtTokenProvider.getTokenExpirationTime(accessToken))
                .build();
    }

    /**
     * 토큰 갱신 구현
     * Refresh Token 검증 후 새로운 Access/Refresh Token 발급
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 리프레시 토큰이 포함된 응답
     * @throws UserException 토큰 검증 실패 시 발생
     */
    @Override
    public LoginResponseDto refreshToken(String refreshToken) {
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UserException(UserResponseStatus.INVALID_REFRESH_TOKEN);
        }
        
        // 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        
        // Redis에 저장된 Refresh Token과 비교
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
        
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UserException(UserResponseStatus.INVALID_REFRESH_TOKEN);
        }
        
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserResponseStatus.USER_NOT_FOUND));
        
        // 새 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        
        // 기존 Refresh Token 삭제
        redisTemplate.delete(refreshTokenKey);
        
        // 새 Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(
                refreshTokenKey,
                newRefreshToken,
                jwtTokenProvider.getTokenExpirationTime(newRefreshToken),
                TimeUnit.SECONDS
        );
        
        // 응답 생성
        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(jwtTokenProvider.getTokenExpirationTime(newAccessToken))
                .build();
    }

    /**
     * 로그아웃 처리 구현
     * Access Token 블랙리스트 추가 및 Refresh Token 삭제
     * 
     * @param accessToken 로그아웃할 액세스 토큰
     * @throws UserException 토큰이 유효하지 않을 경우 발생
     */
    @Override
    public void logout(String accessToken) {
        // 토큰에서 Bearer 제거
        String token = jwtTokenProvider.resolveToken(accessToken);
        if (token == null) {
            throw new UserException(UserResponseStatus.INVALID_TOKEN);
        }
        
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UserException(UserResponseStatus.INVALID_TOKEN);
        }
        
        // 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        
        // Refresh Token 삭제
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(refreshTokenKey);
        
        // Access Token 블랙리스트에 추가
        long expiration = jwtTokenProvider.getTokenExpirationTime(token);
        String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
        
        redisTemplate.opsForValue().set(blacklistKey, "logout", expiration, TimeUnit.SECONDS);
        
        log.info("로그아웃 성공. 사용자: {}", userId);
    }

    /**
     * 앱 백그라운드 전환 처리 구현
     * 현재 사용 중인 Access Token을 블랙리스트에 추가
     * 
     * @param accessToken 현재 사용 중인 액세스 토큰
     * @throws UserException 토큰이 유효하지 않을 경우 발생
     */
    @Override
    public void handleBackground(String accessToken) {
        // 토큰에서 Bearer 제거
        String token = jwtTokenProvider.resolveToken(accessToken);
        if (token == null) {
            throw new UserException(UserResponseStatus.INVALID_TOKEN);
        }
        
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UserException(UserResponseStatus.INVALID_TOKEN);
        }
        
        // Access Token 블랙리스트에 추가
        long expiration = jwtTokenProvider.getTokenExpirationTime(token);
        String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
        
        redisTemplate.opsForValue().set(blacklistKey, "background", expiration, TimeUnit.SECONDS);
        
        log.info("백그라운드 전환 처리 성공. 토큰 무효화.");
    }

    /**
     * 사용자 ID로 사용자 조회 구현
     * 주로 Gateway에서 인증 확인용으로 사용
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     * @throws UserException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    public User getUserForAuth(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserResponseStatus.USER_NOT_FOUND));
    }
}
