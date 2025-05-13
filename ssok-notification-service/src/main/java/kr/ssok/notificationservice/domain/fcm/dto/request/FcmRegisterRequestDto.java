package kr.ssok.notificationservice.domain.fcm.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
public class FcmRegisterRequestDto {
    private String token;    // FCM 토큰
}