package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.client.AligoClient;
import kr.ssok.userservice.client.BankClient;
import kr.ssok.userservice.dto.request.AligoVerificationRequestDto;
import kr.ssok.userservice.dto.request.BankAccountRequestDto;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.BankAccountResponseDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BankClient bankClient;
    private final AligoClient aligoClient;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate redisTemplate;

    @Override
    @Transactional
    public SignupResponseDto registerUser(SignupRequestDto requestDto, BindingResult bindingResult) {
        // 입력값 검증
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldError();
            String errorMessage = error != null ? error.getDefaultMessage() : "유효성 검증 오류";
            log.error("Validation 에러: {}", errorMessage);
            throw new UserException(UserResponseStatus.INVALID_PIN_CODE);
        }
        
        // 중복 가입 확인
        if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new UserException(UserResponseStatus.USER_ALREADY_EXISTS);
        }
        
        // User 엔티티 생성 및 저장
        User user = User.builder()
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .birthDate(requestDto.getBirthDate())
                .pinCode(passwordEncoder.encode(String.valueOf(requestDto.getPinCode()))) // int -> String 변환
                .build();
        
        User savedUser = userRepository.save(user);
        
        try {
            createAccountByBank(requestDto);

            // 응답 생성 (hashedUserCode 포함)
            return SignupResponseDto.builder()
                    .userId(savedUser.getId())
                    .build();

        } catch (Exception e) {
            log.error("계좌 생성 중 error: {}", e.getMessage());
            throw new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        }
    }

    private void createAccountByBank(SignupRequestDto requestDto) {
        // 뱅크 서버에 계좌 개설 요청
        BankAccountRequestDto bankRequest = BankAccountRequestDto.builder()
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .accountTypeCode(1) // 1 예금 고정. 확장 필요 시 수정
                .build();

        // Feign Client를 통한 계좌 개설 요청
        BankAccountResponseDto bankResponse = bankClient.createAccount(bankRequest);
        log.info("계좌 생성 성공: {}", bankResponse.getAccountNumber());
    }

    // 1. 휴대폰 본인 인증
    @Override
    @Transactional
    public void phoneVerification(String phoneNumber) {
        // 인증코드 생성
        String verificationCode = generateVerificationCode();
        log.info(verificationCode + ": 인증번호 생성");

        // phoneNumber로 문자메시지 인증코드 전송 (알리고 문자보내기 API 사용)
        AligoVerificationRequestDto requestDto = AligoVerificationRequestDto.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(verificationCode)
                .build();

        // Feign Client를 통한 계좌 개설 요청
        aligoClient.sendVerificationCode(requestDto);
        log.info(phoneNumber + " " + verificationCode + ": 인증코드 전송");

        // 인증코드 redis 저장 (유효시간 3분)
        redisTemplate.opsForValue().set(phoneNumber, verificationCode, 3, TimeUnit.MINUTES);
    }

    // 1-2 인증번호 생성
    private String generateVerificationCode() {
        log.info("인증번호 Random 값 6자리 생성");
        return String.valueOf(new Random().nextInt(899999) + 100000); // 6자리 랜덤 숫자 생성
    }

    // 인증코드 확인
    @Override
    public boolean verifyCode(String phoneNumber, String verificationCode) {
        String codeInRedis = (String) redisTemplate.opsForValue().get(phoneNumber);

        if (codeInRedis != null && codeInRedis.equals(verificationCode)) {
            redisTemplate.delete(phoneNumber);
            log.info("인증번호 검증 완료, Redis 인증번호 값 삭제");
            return true;
        } else {
            return false;
        }
    }
}
