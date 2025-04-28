package kr.ssok.userservice.service;

import jakarta.validation.Valid;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import org.springframework.validation.BindingResult;

public interface UserService {

    // 회원가입
    SignupResponseDto registerUser(@Valid SignupRequestDto requestDto, BindingResult bindingResult);

    // 휴대폰 인증
    void phoneVerification(String phoneNumber);

    // 인증번호 검증
    boolean verifyCode(String phoneNumber, String verificationCode);
}
