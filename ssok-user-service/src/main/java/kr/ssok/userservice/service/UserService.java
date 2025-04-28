package kr.ssok.userservice.service;

import jakarta.validation.Valid;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import org.springframework.validation.BindingResult;

/**
 * 사용자 관련 서비스 인터페이스
 * 회원가입, 휴대폰 인증, 인증코드 검증 등의 비즈니스 로직을 정의합니다.
 */
public interface UserService {

    /**
     * 회원가입 서비스
     * 사용자 정보를 검증하고 회원 정보를 저장하며, 계좌를 생성합니다.
     * 
     * @param requestDto 회원가입에 필요한 사용자 정보
     * @param bindingResult 유효성 검증 결과
     * @return 회원가입 성공 정보가 담긴 DTO
     */
    SignupResponseDto registerUser(@Valid SignupRequestDto requestDto, BindingResult bindingResult);

    /**
     * 휴대폰 인증 서비스
     * 인증코드를 생성하고 SMS로 발송하며, Redis에 인증코드를 저장합니다.
     * 
     * @param phoneNumber 인증코드를 받을 휴대폰 번호
     */
    void phoneVerification(String phoneNumber);

    /**
     * 인증번호 검증 서비스
     * 사용자가 입력한 인증번호와 Redis에 저장된 인증번호를 비교합니다.
     * 
     * @param phoneNumber 인증을 요청한 휴대폰 번호
     * @param verificationCode 사용자가 입력한 인증코드
     * @return 인증코드 일치 여부 (true: 일치, false: 불일치)
     */
    boolean verifyCode(String phoneNumber, String verificationCode);
}
