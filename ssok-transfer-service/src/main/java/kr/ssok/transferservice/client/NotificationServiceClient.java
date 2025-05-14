package kr.ssok.transferservice.client;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.dto.request.FcmNotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 삭제 예정
@FeignClient(name = "notification-service", url = "${external.notification-service.url}")
public interface NotificationServiceClient {

    /**
     * 수신자에게 푸시 알림 전송 요청
     * @param requestDto 알림 요청 DTO
     * @return 응답
     */
    @PostMapping("/api/notification/fcm/send")
    ResponseEntity<BaseResponse<Void>> sendFcmNotification(@RequestBody FcmNotificationRequestDto requestDto);
}