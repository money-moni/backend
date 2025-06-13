package kr.ssok.transferservice.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 알림 서비스로 보내는 FCM 알림 요청 DTO(삭제 예정)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationRequestDto {
    private Long userId;
    private String title;
    private String body;
    private Map<String, String> data;
}