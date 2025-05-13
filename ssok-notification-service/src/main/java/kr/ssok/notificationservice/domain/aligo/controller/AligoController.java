package kr.ssok.notificationservice.domain.aligo.controller;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.CommonResponseStatus;
import kr.ssok.notificationservice.domain.aligo.dto.AligoVerificationRequestDto;
import kr.ssok.notificationservice.domain.aligo.service.AligoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알리고 SMS 서비스 관련 API를 처리하는 컨트롤러
 * 인증코드 SMS 발송 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class AligoController {
    private final AligoService aligoService;

    /**
     * 인증코드 SMS 발송 API
     * 사용자의 휴대폰 번호로 인증코드가 포함된 SMS를 발송합니다.
     * 
     * @param requestDto 휴대폰 번호와 인증코드가 포함된 요청 DTO
     * @return SMS 발송 결과
     */
    @PostMapping("/verify")
    ResponseEntity<BaseResponse<Void>> sendVerificationCode(@RequestBody AligoVerificationRequestDto requestDto) {
        aligoService.sendVerificationCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode());
        return ResponseEntity.ok(new BaseResponse<>(CommonResponseStatus.SUCCESS));
    }
}
