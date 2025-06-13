package kr.ssok.notificationservice.domain.fcm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 외부 서비스에서 알림 요청 시 사용하는 DTO(삭제 예정)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationRequestDto {
    private Long userId;
    private String title;
    private String body;
    private Map<String, String> data;   // link url 관련 데이터
}