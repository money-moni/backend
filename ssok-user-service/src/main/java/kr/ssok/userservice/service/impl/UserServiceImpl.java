package kr.ssok.userservice.service.impl;

import kr.ssok.common.logging.annotation.ServiceLogging;
import kr.ssok.userservice.client.AligoClient;
import kr.ssok.userservice.client.BankClient;
import kr.ssok.userservice.constants.ProfileConstants;
import kr.ssok.userservice.dto.request.AligoVerificationRequestDto;
import kr.ssok.userservice.dto.request.BankAccountRequestDto;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.BankAccountResponseDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.dto.response.UserInfoResponseDto;
import kr.ssok.userservice.entity.ProfileImage;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.ProfileImageRepository;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.service.S3FileService;
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

/**
 * UserService 인터페이스 구현체
 * 회원가입, 휴대폰 인증, 인증코드 검증 등의 비즈니스 로직을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ServiceLogging(logParameters = true, logResult = false, logExecutionTime = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final S3FileService s3FileService;
    private final ProfileImageRepository profileImageRepository;
    private final BankClient bankClient;
    private final AligoClient aligoClient;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate redisTemplate;

    /**
     * 회원가입 구현
     * 1. 입력값 유효성 검증
     * 2. 중복 가입 확인
     * 3. User 엔티티 생성 및 저장
     * 4. BankClient를 통한 계좌 생성
     * 
     * @param requestDto 회원가입 요청 정보
     * @param bindingResult 유효성 검증 결과
     * @return 회원가입 성공 정보 (사용자 ID)
     * @throws UserException 유효성 검증 실패, 중복 가입, 계좌 생성 실패 등의 예외 발생 시
     */
    @Override
    @Transactional
    public SignupResponseDto registerUser(SignupRequestDto requestDto, BindingResult bindingResult) {
        // 입력값 검증
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldError();
            String errorMessage = error != null ? error.getDefaultMessage() : "유효성 검증 오류";
            log.error("Validation 에러: {}", errorMessage);
            throw new UserException(UserResponseStatus.INVALID_SIGNUP_REQUEST_VALUE);
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

        String fileUrl = s3FileService.getFileUrl(ProfileConstants.DEFAULT_IMAGE_FILENAME);

        // noImage(기본 이미지) 객체 생성
        ProfileImage profileImage = ProfileImage.builder()
                .user(savedUser)
                .storedFilename(ProfileConstants.DEFAULT_IMAGE_FILENAME)
                .url(fileUrl)
                .contentType(ProfileConstants.DEFAULT_IMAGE_CONTENT_TYPE)
                .build();

        profileImageRepository.save(profileImage);

        savedUser.updateProfileImage(profileImage);

        try {
            createAccountByBank(requestDto, "0");

            return SignupResponseDto.builder()
                    .userId(savedUser.getId())
                    .build();

        } catch (Exception e) {
            log.error("계좌 생성 중 error: {}", e.getMessage());
            throw new UserException(UserResponseStatus.BANK_SERVER_ERROR);
        }
    }



    /**
     * 휴대폰 본인 인증 구현
     * 1. 6자리 랜덤 인증코드 생성
     * 2. 알리고 서비스를 통한 SMS 발송
     * 3. Redis에 인증코드 저장 (유효시간 3분)
     * 
     * @param phoneNumber 인증코드를 받을 휴대폰 번호
     */
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

        // Feign Client를 통한 인증번호 발송 요청
        aligoClient.sendVerificationCode(requestDto);
        log.info(phoneNumber + " " + verificationCode + ": 인증코드 전송");

        // 인증코드 redis 저장 (유효시간 3분)
        redisTemplate.opsForValue().set(phoneNumber, verificationCode, 3, TimeUnit.MINUTES);
    }

    /**
     * 인증번호 생성
     * 6자리 랜덤 숫자(100000-999999)를 생성합니다.
     * 
     * @return 생성된 6자리 인증번호
     */
    private String generateVerificationCode() {
        log.info("인증번호 Random 값 6자리 생성");
        return String.valueOf(new Random().nextInt(899999) + 100000); // 6자리 랜덤 숫자 생성
    }

    /**
     * 인증코드 확인 구현
     * Redis에서 휴대폰 번호를 키로 저장된 인증코드를 조회하고 일치 여부를 확인합니다.
     * 인증 성공 시 Redis에서 해당 키를 삭제합니다.
     * 
     * @param phoneNumber 인증을 요청한 휴대폰 번호
     * @param verificationCode 사용자가 입력한 인증코드
     * @return 인증코드 일치 여부 (true: 일치, false: 불일치 또는 만료)
     */
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

    /**
     * PIN 번호 변경을 위한 핸드폰 인증 구현
     * 1. 앱에 저장되어있던 userId로 DB 내 user phoneNumber 검증, 조회
     * 2. 6자리 랜덤 인증코드 생성
     * 3. 알리고 서비스를 통한 SMS 발송
     * 4. Redis에 인증코드 저장 (유효시간 3분)
     * @param userId 앱 내에 저장되어있던 userId
     */
    @Override
    public void requestPinVerification(Long userId) {
        User user = getUserFromRepository(userId);
        String phoneNumber = user.getPhoneNumber();

        phoneVerification(phoneNumber);
    }

    /**
     * PIN 번호 변경을 위한 인증코드 확인
     * @param phoneNumber      전화번호
     * @param verificationCode 인증코드
     * @param userId           사용자 ID
     * @return 인증 여부 boolean
     */
    @Override
    public boolean verifyCodeForPinChange(String phoneNumber, String verificationCode, long userId) {
        // 1. 사용자 존재 여부 확인
        User user = getUserFromRepository(userId);

        // 2. 요청된 전화번호가 사용자의 전화번호와 일치하는지 확인
        if (!user.getPhoneNumber().equals(phoneNumber)) {
            throw new UserException(UserResponseStatus.PHONE_NUMBER_MISMATCH);
        }

        // 3. 기존 verifyCode 메서드로 인증코드 검증
        boolean isValid = verifyCode(phoneNumber, verificationCode);

        // 4. 인증 성공 시 PIN 변경 권한 부여 (Redis에 저장)
        if (isValid) {
            String pinAuthKey = "pin:auth:" + userId;
            redisTemplate.opsForValue().set(pinAuthKey, "true", 5, TimeUnit.MINUTES);
            log.info("PIN 변경 인증 성공. 사용자: {}", userId);
        }

        return isValid;
    }

    /**
     * PIN 번호 변경 서비스
     * @param userId  앱 내에 저장해놓은 userId
     * @param pinCode 사용자에게 입력받은 pinCode
     */
    @Override
    @Transactional
    public void updatePinCode(Long userId, String pinCode) {
        // 1. PIN 변경 권한 확인
        String pinAuthKey = "pin:auth:" + userId;
        Boolean hasAuth = Boolean.TRUE.equals(redisTemplate.hasKey(pinAuthKey));

        if (!hasAuth) {
            throw new UserException(UserResponseStatus.PIN_CHANGE_AUTH_REQUIRED);
        }

        // 2. 사용자 조회
        User user = getUserFromRepository(userId);

        // 3. PIN 코드 암호화
        String encodedPinCode = passwordEncoder.encode(pinCode);

        // 4. PIN 코드 업데이트
        user.updatePinCode(encodedPinCode);

        // 5. 인증 정보 삭제 (1회성)
        redisTemplate.delete(pinAuthKey);
    }


    /**
     * 특정 유저 정보 조회 서비스
     * @param userId Gateway에서 전달한 사용자 ID (헤더)
     * @return 유저 정보 조회 결과 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(long userId) {
        User user = getUserFromRepository(userId);

        return UserInfoResponseDto.builder()
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage().getUrl())
                .build();
    }

    //

    /**
     * 뱅크 서버를 통한 계좌 생성
     * Feign Client를 사용하여 뱅크 서비스에 계좌 개설을 요청합니다.
     *
     * @param requestDto 회원가입 요청 정보 (계좌 생성에 필요한 이름, 전화번호 포함)
     */
    private void createAccountByBank(SignupRequestDto requestDto, String userTypeCode) {
        // 뱅크 서버에 계좌 개설 요청
        BankAccountRequestDto bankRequest = BankAccountRequestDto.builder()
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .accountTypeCode(0) // 1 예금 고정. 확장 필요 시 수정
                .userTypeCode(userTypeCode)
                .build();

        // Feign Client를 통한 계좌 개설 요청
        BankAccountResponseDto bankResponse = bankClient.createAccount(bankRequest);
        log.info("계좌 생성 성공: {}", bankResponse.getAccountNumber());
    }

    /**
     * userRepository에서 userId로 User를 조회합니다.
     * @param userId User 식별자
     * @return User 객체
     */
    private User getUserFromRepository(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserException(UserResponseStatus.USER_NOT_FOUND));
        return user;
    }
}
