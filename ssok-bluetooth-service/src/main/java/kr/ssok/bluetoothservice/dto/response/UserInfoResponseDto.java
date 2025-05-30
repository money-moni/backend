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

    private String uuid;          // 유저 블루투스 uuid
    private String username;      // 유저 이름 (마스킹 처리)
    private String phoneSuffix;   // 핸드폰 뒷 4자리
    private String profileImage;  // 프로필 이미지 URL (S3 경로 등)
}