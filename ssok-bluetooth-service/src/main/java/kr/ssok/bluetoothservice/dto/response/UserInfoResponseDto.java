package kr.ssok.bluetoothservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponseDto {

    private Long userId;          // 유저 ID
    private String username;      // 유저 이름 (마스킹 처리)
    private String profileImage;  // 프로필 이미지 URL (S3 경로 등)
}