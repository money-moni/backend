package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.client.BankClient;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BankClient bankClient;
    private final PasswordEncoder passwordEncoder;

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
            // 뱅크 서버에 계좌 개설 요청
            BankAccountRequestDto bankRequest = BankAccountRequestDto.builder()
                    .username(requestDto.getUsername())
                    .phoneNumber(requestDto.getPhoneNumber())
                    .accountTypeCode(1) // 1 예금 고정. 확장 필요 시 수정
                    .build();
            
            // Feign Client를 통한 계좌 개설 요청
            BankAccountResponseDto bankResponse = bankClient.createAccount(bankRequest);
            log.info("계좌 생성 성공: {}", bankResponse.getAccountNumber());
            
            // 응답 생성 (hashedUserCode 포함)
            return SignupResponseDto.builder()
                    .userId(savedUser.getId())
                    .build();
            
        } catch (Exception e) {
            log.error("계좌 생성 중 error: {}", e.getMessage());
            throw new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        }
    }
}
