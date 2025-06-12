package kr.ssok.userservice.service;

import jakarta.validation.Valid;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.dto.response.PhoneVerificationResponseDto;
import kr.ssok.userservice.dto.response.UserInfoResponseDto;
import org.springframework.validation.BindingResult;

/**
 * 사용자 관련 서비스 인터페이스
 * 회원가입, 휴대폰 인증, 인증코드 검증 등의 비즈니스 로직을 정의합니다.
 */
public interface UserService {

    /**
     * 회원가입 서비스
     * 사용자 정보를 검증하고 회원 정보를 저장하며, 계좌를 생성합니다.
     * 휴대폰 인증이 완료된 상태에서만 회원가입이 가능합니다.
     *
     * @param requestDto    회원가입에 필요한 사용자 정보
     * @param bindingResult 유효성 검증 결과
     * @return 회원가입 성공 정보가 담긴 DTO
     */
    SignupResponseDto registerUser(@Valid SignupRequestDto requestDto, BindingResult bindingResult);

    /**
     * PIN 재등록 (기존 사용자용)
     * 앱 재설치 등으로 인해 기존 사용자가 PIN을 재등록할 때 사용합니다.
     * 휴대폰 인증이 완료된 상태에서만 PIN 재등록이 가능합니다.
     * 
     * @param userId 기존 사용자 ID
     * @param pinCode 새로운 PIN 코드
     */
    void reRegisterPinForExistingUser(Long userId, String pinCode);

    /**
     * 휴대폰 인증 서비스
     * 인증코드를 생성하고 SMS로 발송하며, Redis에 인증코드를 저장합니다.
     *
     * @param phoneNumber 인증코드를 받을 휴대폰 번호
     */
    void phoneVerification(String phoneNumber);

    /**
     * 인증번호 검증 서비스 (기존 사용자 확인 포함)
     * 사용자가 입력한 인증번호와 Redis에 저장된 인증번호를 비교하고,
     * 인증 성공 시 해당 휴대폰 번호로 등록된 사용자가 있는지 확인합니다.
     *
     * @param phoneNumber      인증을 요청한 휴대폰 번호
     * @param verificationCode 사용자가 입력한 인증코드
     * @return 인증 결과 및 기존 사용자 정보
     */
    PhoneVerificationResponseDto verifyCodeWithUserCheck(String phoneNumber, String verificationCode);

    /**
     * 인증번호 검증 서비스
     * 사용자가 입력한 인증번호와 Redis에 저장된 인증번호를 비교합니다.
     *
     * @param phoneNumber      인증을 요청한 휴대폰 번호
     * @param verificationCode 사용자가 입력한 인증코드
     * @return 인증코드 일치 여부 (true: 일치, false: 불일치)
     */
    boolean verifyCode(String phoneNumber, String verificationCode);

    /**
     * PIN 번호 변경을 위한 핸드폰 인증 서비스
     * 인증코드를 생성하고 SMS로 발송하며, Redis에 인증코드를 저장합니다.
     *
     * @param userId 앱 내에 저장해놓은 userId
     */
    void requestPinVerification(Long userId);


    /**
     * PIN 번호 변경 서비스
     *
     * @param userId  앱 내에 저장해놓은 userId
     * @param pinCode 사용자에게 입력받은 pinCode
     */
    void updatePinCode(Long userId, String pinCode);


    /**
     * 특정 유저 정보 조회 서비스
     *
     * @param userId Gateway에서 전달한 사용자 ID (헤더)
     * @return 유저 정보 조회 결과가 담긴 DTO
     */
    UserInfoResponseDto getUserInfo(long userId);


    /**
     * PIN 번호 변경을 위한 인증코드 확인
     *
     * @param phoneNumber      전화번호
     * @param verificationCode 인증코드
     * @param userId           사용자 ID
     * @return 인증 성공 여부
     */
    boolean verifyCodeForPinChange(String phoneNumber, String verificationCode, long userId);

    // pin 재등록
    void reRegisterPinCode(String userId);
}
