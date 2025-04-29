package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.client.AligoClient;
import kr.ssok.userservice.client.BankClient;
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
import kr.ssok.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankClient bankClient;

    @Mock
    private AligoClient aligoClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Redis ValueOperations 모킹 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * 회원가입 성공 시 사용자가 저장되고 계좌가 생성되며, 응답이 올바르게 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUser_Success() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        User savedUser = User.builder()
                .id(1L)
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .birthDate(requestDto.getBirthDate())
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(String.valueOf(requestDto.getPinCode()))).thenReturn("encodedPinCode");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        BankAccountResponseDto bankResponse = new BankAccountResponseDto();
        bankResponse.setAccountNumber("123-456-789");
        when(bankClient.createAccount(any(BankAccountRequestDto.class))).thenReturn(bankResponse);

        // when
        SignupResponseDto responseDto = userService.registerUser(requestDto, bindingResult);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUserId()).isEqualTo(1L);
        
        // verify
        verify(userRepository).existsByPhoneNumber(requestDto.getPhoneNumber());
        verify(passwordEncoder).encode(String.valueOf(requestDto.getPinCode()));
        verify(userRepository).save(any(User.class));
        verify(bankClient).createAccount(any(BankAccountRequestDto.class));
        
        // 저장된 User 객체 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        
        assertThat(capturedUser.getUsername()).isEqualTo(requestDto.getUsername());
        assertThat(capturedUser.getPhoneNumber()).isEqualTo(requestDto.getPhoneNumber());
        assertThat(capturedUser.getBirthDate()).isEqualTo(requestDto.getBirthDate());
        assertThat(capturedUser.getPinCode()).isEqualTo("encodedPinCode");
    }

    /**
     * 이미 등록된 전화번호로 회원가입 시도 시 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("중복된 전화번호로 회원가입 시도 시 예외 발생 테스트")
    void registerUser_DuplicatePhone_ThrowsException() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        when(userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())).thenReturn(true);

        // when & then
        UserException exception = assertThrows(UserException.class, 
                () -> userService.registerUser(requestDto, bindingResult));
        
        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.USER_ALREADY_EXISTS);
        
        // verify
        verify(userRepository).existsByPhoneNumber(requestDto.getPhoneNumber());
        verify(userRepository, never()).save(any(User.class));
        verify(bankClient, never()).createAccount(any(BankAccountRequestDto.class));
    }

    /**
     * 유효하지 않은 PIN 코드로 회원가입 시도 시 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("유효하지 않은 PIN 코드로 회원가입 시도 시 예외 발생 테스트")
    void registerUser_InvalidPinCode_ThrowsException() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(12345) // 5자리 PIN 코드 (유효하지 않음)
                .build();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        
        FieldError fieldError = new FieldError("signupRequestDto", "pinCode", "PIN 코드는 6자리여야 합니다");
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        // when & then
        UserException exception = assertThrows(UserException.class, 
                () -> userService.registerUser(requestDto, bindingResult));
        
        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.INVALID_PIN_CODE);
        
        // verify
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(bankClient, never()).createAccount(any(BankAccountRequestDto.class));
    }

    /**
     * 계좌 생성 중 뱅크 서버 오류 발생 시 사용자 등록은 되지만 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("계좌 생성 중 뱅크 서버 오류 발생 시 예외 발생 테스트")
    void registerUser_BankServerError_ThrowsException() {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        User savedUser = User.builder()
                .id(1L)
                .username(requestDto.getUsername())
                .phoneNumber(requestDto.getPhoneNumber())
                .birthDate(requestDto.getBirthDate())
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(String.valueOf(requestDto.getPinCode()))).thenReturn("encodedPinCode");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // 뱅크 서버 오류 시뮬레이션
        when(bankClient.createAccount(any(BankAccountRequestDto.class))).thenThrow(new RuntimeException("뱅크 서버 연결 실패"));

        // when & then
        UserException exception = assertThrows(UserException.class, 
                () -> userService.registerUser(requestDto, bindingResult));
        
        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.BANK_SERVER_ERROR);
        
        // verify
        verify(userRepository).existsByPhoneNumber(requestDto.getPhoneNumber());
        verify(passwordEncoder).encode(String.valueOf(requestDto.getPinCode()));
        verify(userRepository).save(any(User.class));
        verify(bankClient).createAccount(any(BankAccountRequestDto.class));
    }

    /**
     * 정상적으로 휴대폰 인증코드가 발송되고 Redis에 저장되는지 검증합니다.
     */
    @Test
    @DisplayName("휴대폰 인증코드 발송 성공 테스트")
    void phoneVerification_Success() {
        // given
        String phoneNumber = "01012345678";
        
        // when
        userService.phoneVerification(phoneNumber);
        
        // then
        // 알리고 클라이언트로 인증코드 발송 요청 검증
        verify(aligoClient).sendVerificationCode(any(AligoVerificationRequestDto.class));
        
        // Redis에 인증코드 저장 검증
        verify(valueOperations).set(eq(phoneNumber), anyString(), eq(3L), eq(TimeUnit.MINUTES));
        
        // ArgumentCaptor로 실제 저장된 인증코드 검증
        ArgumentCaptor<AligoVerificationRequestDto> aligoCaptor = ArgumentCaptor.forClass(AligoVerificationRequestDto.class);
        verify(aligoClient).sendVerificationCode(aligoCaptor.capture());
        
        AligoVerificationRequestDto capturedDto = aligoCaptor.getValue();
        assertThat(capturedDto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(capturedDto.getVerificationCode()).isNotNull();
        assertThat(capturedDto.getVerificationCode().length()).isEqualTo(6);
        
        // Redis에 저장된 값 검증
        ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(phoneNumber), redisValueCaptor.capture(), eq(3L), eq(TimeUnit.MINUTES));
        
        String capturedVerificationCode = redisValueCaptor.getValue();
        assertThat(capturedVerificationCode).isEqualTo(capturedDto.getVerificationCode());
    }

    /**
     * Redis에 저장된 인증코드와 입력값이 일치할 때 인증 성공 여부를 검증합니다.
     */
    @Test
    @DisplayName("인증코드 검증 성공 테스트")
    void verifyCode_Success() {
        // given
        String phoneNumber = "01012345678";
        String verificationCode = "123456";
        
        when(valueOperations.get(phoneNumber)).thenReturn(verificationCode);
        
        // when
        boolean result = userService.verifyCode(phoneNumber, verificationCode);
        
        // then
        assertThat(result).isTrue();
        
        // Redis에서 해당 키 삭제 검증
        verify(redisTemplate).delete(phoneNumber);
    }

    /**
     * 인증코드가 불일치할 경우 인증이 실패하며, Redis에서 삭제되지 않는지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 검증 실패 - 잘못된 코드 테스트")
    void verifyCode_IncorrectCode_ReturnsFalse() {
        // given
        String phoneNumber = "01012345678";
        String storedCode = "123456";
        String inputCode = "654321"; // 잘못된 코드
        
        when(valueOperations.get(phoneNumber)).thenReturn(storedCode);
        
        // when
        boolean result = userService.verifyCode(phoneNumber, inputCode);
        
        // then
        assertThat(result).isFalse();
        
        // Redis에서 키를 삭제하지 않음을 검증
        verify(redisTemplate, never()).delete(phoneNumber);
    }

    /**
     * 인증코드가 만료된 경우(null 반환) 인증이 실패하는지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 검증 실패 - 코드 만료 테스트")
    void verifyCode_ExpiredCode_ReturnsFalse() {
        // given
        String phoneNumber = "01012345678";
        String inputCode = "123456";
        
        // Redis에 저장된 코드가 없는 경우 (만료됨)
        when(valueOperations.get(phoneNumber)).thenReturn(null);
        
        // when
        boolean result = userService.verifyCode(phoneNumber, inputCode);
        
        // then
        assertThat(result).isFalse();
        
        // Redis에서 키를 삭제하지 않음을 검증
        verify(redisTemplate, never()).delete(phoneNumber);
    }

    /**
     * PIN 번호 변경을 위한 인증 요청 시 휴대폰 번호로 인증코드를 발송하고 Redis에 저장되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증 요청 성공 테스트")
    void requestPinVerification_Success() {
        // given
        Long userId = 1L;
        String phoneNumber = "01012345678";

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber(phoneNumber)
                .birthDate("19900101")
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.requestPinVerification(userId);

        // then
        // 사용자 조회 검증
        verify(userRepository).findById(userId);

        // 알리고 클라이언트로 인증코드 발송 요청 검증
        verify(aligoClient).sendVerificationCode(any(AligoVerificationRequestDto.class));

        // Redis에 인증코드 저장 검증
        verify(valueOperations).set(eq(phoneNumber), anyString(), eq(3L), eq(TimeUnit.MINUTES));
    }

    /**
     * 존재하지 않는 사용자 ID로 PIN 인증 요청 시 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 PIN 번호 변경 인증 요청 시 예외 발생 테스트")
    void requestPinVerification_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L; // 존재하지 않는 ID

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        UserException exception = assertThrows(UserException.class,
                () -> userService.requestPinVerification(userId));

        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.USER_NOT_FOUND);

        // 사용자 조회 검증
        verify(userRepository).findById(userId);

        // 인증코드 발송 및 저장이 호출되지 않음을 검증
        verify(aligoClient, never()).sendVerificationCode(any(AligoVerificationRequestDto.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    /**
     * PIN 번호 변경을 위한 인증코드 검증 시 성공하는 경우 Redis에서 삭제되고 권한이 저장되는지 확인합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증코드 확인 성공 테스트")
    void verifyCodeForPinChange_Success() {
        // given
        Long userId = 1L;
        String phoneNumber = "01012345678";
        String verificationCode = "123456";

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber(phoneNumber)
                .birthDate("19900101")
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(valueOperations.get(phoneNumber)).thenReturn(verificationCode);

        // when
        boolean result = userService.verifyCodeForPinChange(phoneNumber, verificationCode, userId);

        // then
        assertThat(result).isTrue();

        // 사용자 조회 검증
        verify(userRepository).findById(userId);

        // Redis에서 인증코드 조회 및 삭제 검증
        verify(valueOperations).get(phoneNumber);
        verify(redisTemplate).delete(phoneNumber);

        // PIN 변경 권한 저장 검증
        verify(valueOperations).set(eq("pin:auth:" + userId), eq("true"), eq(5L), eq(TimeUnit.MINUTES));
    }

    /**
     * 인증 요청 시 사용자의 전화번호와 요청 전화번호가 다를 경우 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("전화번호 불일치 시 PIN 번호 변경 인증 예외 발생 테스트")
    void verifyCodeForPinChange_PhoneNumberMismatch_ThrowsException() {
        // given
        Long userId = 1L;
        String userPhoneNumber = "01012345678";
        String requestPhoneNumber = "01087654321"; // 불일치하는 전화번호
        String verificationCode = "123456";

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber(userPhoneNumber)
                .birthDate("19900101")
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        UserException exception = assertThrows(UserException.class,
                () -> userService.verifyCodeForPinChange(requestPhoneNumber, verificationCode, userId));

        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.PHONE_NUMBER_MISMATCH);

        // 사용자 조회 검증
        verify(userRepository).findById(userId);

        // Redis 작업이 호출되지 않음을 검증
        verify(valueOperations, never()).get(anyString());
        verify(redisTemplate, never()).delete(anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    /**
     * PIN 변경 인증코드가 불일치할 경우 인증이 실패하며, Redis 키 삭제 및 권한 저장이 되지 않는지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 불일치 시 PIN 번호 변경 인증 실패 테스트")
    void verifyCodeForPinChange_IncorrectCode_ReturnsFalse() {
        // given
        Long userId = 1L;
        String phoneNumber = "01012345678";
        String storedCode = "123456";
        String inputCode = "654321"; // 잘못된 코드

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber(phoneNumber)
                .birthDate("19900101")
                .pinCode("encodedPinCode")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(valueOperations.get(phoneNumber)).thenReturn(storedCode);

        // when
        boolean result = userService.verifyCodeForPinChange(phoneNumber, inputCode, userId);

        // then
        assertThat(result).isFalse();

        // 사용자 조회 검증
        verify(userRepository).findById(userId);

        // Redis에서 인증코드 조회 검증
        verify(valueOperations).get(phoneNumber);

        // Redis 키 삭제 및 PIN 변경 권한 저장이 호출되지 않음을 검증
        verify(redisTemplate, never()).delete(anyString());
        verify(valueOperations, never()).set(eq("pin:auth:" + userId), anyString(), anyLong(), any(TimeUnit.class));
    }

    /**
     * PIN 변경 권한이 있을 때 PIN 번호 변경이 성공적으로 수행되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경 성공 테스트")
    void updatePinCode_Success() {
        // given
        Long userId = 1L;
        String pinCode = "654321";
        String encodedPinCode = "encodedNewPinCode";
        String pinAuthKey = "pin:auth:" + userId;

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode("encodedOldPinCode")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(redisTemplate.hasKey(pinAuthKey)).thenReturn(true);
        when(passwordEncoder.encode(pinCode)).thenReturn(encodedPinCode);

        // when
        userService.updatePinCode(userId, pinCode);

        // then
        // 사용자 조회 및 PIN 변경 권한 확인 검증
        verify(userRepository).findById(userId);
        verify(redisTemplate).hasKey(pinAuthKey);

        // PIN 코드 암호화 검증
        verify(passwordEncoder).encode(pinCode);

        // PIN 변경 권한 삭제 검증
        verify(redisTemplate).delete(pinAuthKey);

        // 사용자 PIN 코드 업데이트 검증
        assertThat(user.getPinCode()).isEqualTo(encodedPinCode);
    }

    /**
     * PIN 변경 권한이 없을 경우 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 변경 권한 없을 시 PIN 번호 변경 예외 발생 테스트")
    void updatePinCode_NoAuth_ThrowsException() {
        // given
        Long userId = 1L;
        String pinCode = "654321";
        String pinAuthKey = "pin:auth:" + userId;

        when(redisTemplate.hasKey(pinAuthKey)).thenReturn(false);

        // when & then
        UserException exception = assertThrows(UserException.class,
                () -> userService.updatePinCode(userId, pinCode));

        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.PIN_CHANGE_AUTH_REQUIRED);

        // PIN 변경 권한 확인 검증
        verify(redisTemplate).hasKey(pinAuthKey);

        // 사용자 조회, PIN 코드 암호화, PIN 변경 권한 삭제가 호출되지 않음을 검증
        verify(userRepository, never()).findById(anyLong());
        verify(passwordEncoder, never()).encode(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    /**
     * 사용자 ID로 사용자 정보를 성공적으로 조회할 수 있는지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    void getUserInfo_Success() {
        // given
        Long userId = 1L;

        ProfileImage profileImage = ProfileImage.builder()
                .id(1L)
                .url("https://example.com/profile.jpg")
                .storedFilename("profile.jpg")
                .contentType("image/jpeg")
                .build();

        User user = User.builder()
                .id(userId)
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode("encodedPinCode")
                .profileImage(profileImage)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserInfoResponseDto responseDto = userService.getUserInfo(userId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUsername()).isEqualTo("홍길동");
        assertThat(responseDto.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(responseDto.getProfileImage()).isEqualTo("https://example.com/profile.jpg");

        // 사용자 조회 검증
        verify(userRepository).findById(userId);
    }

    /**
     * 존재하지 않는 사용자 ID로 정보 조회 시 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 정보 조회 시 예외 발생 테스트")
    void getUserInfo_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L; // 존재하지 않는 ID
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // when & then
        UserException exception = assertThrows(UserException.class, 
                () -> userService.getUserInfo(userId));
        
        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.USER_NOT_FOUND);
        
        // 사용자 조회 검증
        verify(userRepository).findById(userId);
    }
}
