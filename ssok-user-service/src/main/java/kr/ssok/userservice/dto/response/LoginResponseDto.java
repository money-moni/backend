package kr.ssok.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    
    private String accessToken;
    private String refreshToken;
    
    // 토큰 만료 시간 정보(초)
    private long accessTokenExpiresIn;
}
