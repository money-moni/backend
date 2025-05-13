package kr.ssok.notificationservice.domain.fcm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 알림 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageRequestDto {
    private String title;   // 알림 제목 (예: "1,000원 입금")
    private String body;    // 알림 내용 (예: "송신자 -> 내 OO뱅크 통장")
    private String image;   // 알림 이미지 (예: 쏙 송금 앱 이미지)
    private String token;   // FCM 토큰
}
