package kr.ssok.userservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 * 사용자 인증 정보를 기반으로 JWT 토큰을 생성하고 검증합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    private Key key;

    @PostConstruct
    public void init() {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(encodedKey.getBytes());
        log.info("JWT key initialized");
    }

    /**
     * Access Token을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @return 생성된 JWT Access Token
     */
    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityInSeconds * 1000);
    }

    /**
     * Refresh Token을 생성합니다.
     * 
     * @param userId 사용자 ID
     * @return 생성된 JWT Refresh Token
     */
    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityInSeconds * 1000);
    }

    /**
     * 토큰 생성 메소드
     */
    private String createToken(Long userId, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 userId 추출
     * 
     * @param token JWT 토큰
     * @return 토큰에 저장된 사용자 ID
     * @throws UserException 토큰이 유효하지 않을 경우 발생
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.get("userId").toString());
        } catch (Exception e) {
            log.error("Could not get userId from token: {}", e.getMessage());
            throw new UserException(UserResponseStatus.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 유효성 검증
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부 (true: 유효, false: 유효하지 않음)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 검증 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 토큰 남은 유효시간 계산
     * 
     * @param token JWT 토큰
     * @return 토큰 남은 유효시간 (초 단위)
     */
    public long getTokenExpirationTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            return (expiration.getTime() - now.getTime()) / 1000; // 초 단위 변환
        } catch (Exception e) {
            log.error("Could not calculate token expiration time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Bearer 토큰에서 JWT 추출
     * 
     * @param bearerToken Bearer 토큰
     * @return JWT 토큰 (Bearer 프리픽스 제거된)
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
