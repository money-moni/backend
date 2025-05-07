package kr.ssok.bluetoothservice.client.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 유저 정보 DTO
 * - UserServiceClient를 통해 가져오는 사용자 정보 데이터 구조
 */
@Getter
@Builder
public class UserInfoDto {

    private Long userId;          // 유저 ID
    private String username;      // 유저 이름 (마스킹 처리)
    private String profileImage;  // 프로필 이미지 URL (S3 경로 등)
}