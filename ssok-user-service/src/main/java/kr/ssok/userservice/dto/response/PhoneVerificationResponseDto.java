package kr.ssok.userservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneVerificationResponseDto {
    private boolean isExistingUser;  // 기존 사용자 여부
    private Long userId;            // 기존 사용자인 경우 userId, 아닌 경우 null
}
