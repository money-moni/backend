package kr.ssok.notificationservice.domain.fcm.controller;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.notificationservice.domain.fcm.dto.request.FcmRegisterRequestDto;
import kr.ssok.notificationservice.domain.fcm.service.FcmService;
import kr.ssok.notificationservice.global.exception.NotificationResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FCM 토큰 등록 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification/fcm")
public class FcmController {

    private final FcmService fcmService;

    /**
     * FCM 토큰 등록 API
     *
     * @param userId   사용자 ID (헤더)
     * @param requestDto 디바이스. FCM 토큰 정보
     * @return 등록 결과 응답
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> registerFcmToken(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody FcmRegisterRequestDto requestDto) {
        fcmService.registerFcmToken(Long.parseLong(userId), requestDto.getToken());
        return ResponseEntity.ok(new BaseResponse<>(NotificationResponseStatus.TOKEN_REGISTER_SUCCESS));
    }
}