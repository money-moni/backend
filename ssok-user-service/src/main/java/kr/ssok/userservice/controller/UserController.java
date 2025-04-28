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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponseDto>> registerUser(
            @Valid @RequestBody SignupRequestDto requestDto,
            BindingResult bindingResult) {
        SignupResponseDto responseDto = userService.registerUser(requestDto, bindingResult);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(UserResponseStatus.REGISTER_USER_SUCCESS, responseDto));
    }

    // 핸드폰 본인 인증
    @PostMapping("/phone")
    public ResponseEntity<BaseResponse<Void>> phoneVerification(@RequestBody PhoneVerificationRequestDto requestDto) {
        userService.phoneVerification(requestDto.getPhoneNumber());
        return ResponseEntity.ok(new BaseResponse<>(UserResponseStatus.SUCCESS));
    }

    // 인증코드 확인
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
