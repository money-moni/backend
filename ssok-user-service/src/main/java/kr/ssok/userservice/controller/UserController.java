package kr.ssok.userservice.controller;

import jakarta.validation.Valid;
import kr.ssok.common.exception.BaseResponse;
import kr.ssok.userservice.dto.request.PhoneVerificationRequestDto;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 * 회원가입, 휴대폰 인증, 인증코드 확인 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 API
     * 사용자 정보를 받아 회원가입을 진행하고 성공 시 사용자 ID를 반환합니다.
     * 
     * @param requestDto 회원가입에 필요한 사용자 정보 (이름, 전화번호, 생년월일, PIN 코드 등)
     * @param bindingResult 유효성 검증 결과
     * @return 회원가입 성공 시 사용자 ID가 포함된 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponseDto>> registerUser(
            @Valid @RequestBody SignupRequestDto requestDto,
            BindingResult bindingResult) {
        SignupResponseDto responseDto = userService.registerUser(requestDto, bindingResult);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(UserResponseStatus.REGISTER_USER_SUCCESS, responseDto));
    }

    /**
     * 휴대폰 본인 인증 API (가입)
     * 사용자의 휴대폰 번호로 인증 코드를 생성하고 SMS를 발송합니다.
     * 
     * @param requestDto 휴대폰 번호가 포함된 요청 DTO
     * @return 인증 코드 발송 성공 여부
     */
    @PostMapping("/phone")
    public ResponseEntity<BaseResponse<Void>> phoneVerification(@RequestBody PhoneVerificationRequestDto requestDto) {
        userService.phoneVerification(requestDto.getPhoneNumber());
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    /**
     * PIN 번호 변경을 위한 핸드폰 인증 API
     * @param userId 앱 내에 저장되어있던 userId
     * @return 인증 코드 발송 성공 여부
     */
    @PostMapping("/pin/{userId}")
    public ResponseEntity<BaseResponse<Void>> requestPhoneVerificationForPinCode(@PathVariable Long userId) {
        userService.requestPhoneVerificationForPinCode(userId);
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    /**
     * 인증코드 확인 API
     * 사용자가 입력한 인증코드와 Redis에 저장된 인증코드를 비교하여 유효성을 검증합니다.
     * 
     * @param verificationDto 휴대폰 번호와 인증코드가 포함된 요청 DTO
     * @return 인증코드 검증 결과 (성공 또는 실패)
     */
    @PostMapping("/phone/verify")
    public ResponseEntity<BaseResponse<Void>> verifyCode(@RequestBody PhoneVerificationRequestDto verificationDto) {
        boolean isValid = userService.verifyCode(verificationDto.getPhoneNumber(), verificationDto.getVerificationCode());
        if (isValid) {
            return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponse<>(UserResponseStatus.CODE_VERIFICATION_FAIL));
        }
    }
}
