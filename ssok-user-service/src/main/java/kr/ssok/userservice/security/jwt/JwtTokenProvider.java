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
     * Access Token 생성
     */
    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenValidityInSeconds * 1000);
    }

    /**
     * Refresh Token 생성
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
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 남은 유효시간 계산
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
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
