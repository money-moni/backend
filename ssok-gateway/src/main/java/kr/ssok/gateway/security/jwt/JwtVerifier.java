package kr.ssok.gateway.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰 검증 유틸리티 클래스
 * 게이트웨이에서는 토큰 생성 없이 검증만 수행합니다.
 */
@Slf4j
@Component
public class JwtVerifier {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @PostConstruct
    public void init() {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(encodedKey.getBytes());
        log.info("JWT key initialized");
    }

    /**
     * 토큰 유효성 검증
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부 (true: 유효, false: 유효하지 않음)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 토큰에 저장된 사용자 ID, 실패 시 null
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
            return null;
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
