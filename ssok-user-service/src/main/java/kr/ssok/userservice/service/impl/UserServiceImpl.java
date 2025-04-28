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
import kr.ssok.userservice.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public SignupResponseDto registerUser(SignupRequestDto requestDto, BindingResult bindingResult) {
        // 입력값 검증
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldError();
            String errorMessage = error != null ? error.getDefaultMessage() : "유효성 검증 오류";
            log.error("Validation error: {}", errorMessage);
            throw new UserException(UserResponseStatus.INVALID_PIN_CODE);
        }
        
        // 중복 가입 확인
        if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new UserException(UserResponseStatus.USER_ALREADY_EXISTS);
        }
        
        // hashedUserCode 생성
        String hashedUserCode = HashUtil.generateHashedUserCode(
                requestDto.getUsername(),
                requestDto.getBirthDate(),
                requestDto.getPhoneNumber()
        );
        
        // User 엔티티 생성 및 저장
        User user = User.builder()
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .birthDate(requestDto.getBirthDate())
                .pinCode(String.valueOf(requestDto.getPinCode())) // int -> String 변환
//                .gender(Gender.valueOf(requestDto.getGender())) // 요청에서 받은 Gender 사용
                .hashedUserCode(hashedUserCode)
                .build();
        
        User savedUser = userRepository.save(user);
        
        try {
            // 뱅크 서버에 계좌 개설 요청
            BankAccountRequestDto bankRequest = BankAccountRequestDto.builder()
                    .hashedUserCode(hashedUserCode)
                    .phoneNumber(requestDto.getPhoneNumber())
                    .build();
            
            // Feign Client를 통한 계좌 개설 요청
            BankAccountResponseDto bankResponse = bankClient.createAccount(bankRequest);
            log.info("Bank account created successfully: {}", bankResponse.getAccountNumber());
            
            // 응답 생성 (hashedUserCode 포함)
            return SignupResponseDto.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .phoneNumber(savedUser.getPhoneNumber())
                    .accountNumber(bankResponse.getAccountNumber())
                    .hashedUserCode(hashedUserCode)
                    .build();
            
        } catch (Exception e) {
            log.error("Error during bank account creation: {}", e.getMessage());
            throw new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        }
    }
}
