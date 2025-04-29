package kr.ssok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.userservice.config.TestSecurityConfig;
import kr.ssok.userservice.dto.request.PhoneVerificationRequestDto;
import kr.ssok.userservice.dto.request.PinCodeRequestDto;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.dto.response.UserInfoResponseDto;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BindingResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 클래스에 대한 단위 테스트
 * 회원가입, 인증코드 발송 및 확인, PIN 변경 등의 엔드포인트를 테스트합니다.
 */
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    /**
     * 회원가입 엔드포인트 - 성공 테스트
     * 유효한 SignupRequestDto로 요청 시 HTTP 201과 올바른 BaseResponse<SignupResponseDto>가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("회원가입 엔드포인트 - 성공 테스트")
    void registerUser_Success() throws Exception {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        SignupResponseDto responseDto = SignupResponseDto.builder()
                .userId(1L)
                .build();

        when(userService.registerUser(any(SignupRequestDto.class), any(BindingResult.class))).thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.REGISTER_USER_SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.REGISTER_USER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.userId").value(1L));

        // verify
        verify(userService).registerUser(any(SignupRequestDto.class), any(BindingResult.class));
    }

    /**
     * 회원가입 엔드포인트 - 파라미터 검증 테스트
     * UserService.registerUser 메서드 호출 시 전달되는 매개변수가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("회원가입 엔드포인트 - 파라미터 검증 테스트")
    void registerUser_VerifyParameters() throws Exception {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        SignupResponseDto responseDto = SignupResponseDto.builder()
                .userId(1L)
                .build();

        when(userService.registerUser(any(SignupRequestDto.class), any(BindingResult.class))).thenReturn(responseDto);

        // when
        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        ArgumentCaptor<SignupRequestDto> dtoCaptor = ArgumentCaptor.forClass(SignupRequestDto.class);
        verify(userService).registerUser(dtoCaptor.capture(), any(BindingResult.class));

        SignupRequestDto capturedDto = dtoCaptor.getValue();
        assert capturedDto.getUsername().equals("홍길동");
        assert capturedDto.getPhoneNumber().equals("01012345678");
        assert capturedDto.getBirthDate().equals("19900101");
        assert capturedDto.getPinCode() == 123456;
    }

    /**
     * 회원가입 엔드포인트 - 서비스 예외 테스트
     * UserService에서 예외 발생 시 적절한 오류 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("회원가입 엔드포인트 - 서비스 예외 테스트")
    void registerUser_ServiceException() throws Exception {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        when(userService.registerUser(any(SignupRequestDto.class), any(BindingResult.class)))
                .thenThrow(new UserException(UserResponseStatus.USER_ALREADY_EXISTS));

        // when
        ResultActions result = mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isBadRequest()) // UserResponseStatus.USER_ALREADY_EXISTS의 상태코드에 따라 달라질 수 있습니다.
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.USER_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.USER_ALREADY_EXISTS.getMessage()));
    }

    /**
     * 휴대폰 인증코드 발송 엔드포인트 - 성공 테스트
     * 유효한 PhoneVerificationRequestDto로 요청 시 HTTP 200과 성공 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("휴대폰 인증코드 발송 엔드포인트 - 성공 테스트")
    void phoneVerification_Success() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");

        // when
        ResultActions result = mockMvc.perform(post("/api/users/phone")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()));

        // verify
        verify(userService).phoneVerification(requestDto.getPhoneNumber());
    }

    /**
     * 휴대폰 인증코드 발송 엔드포인트 - 파라미터 검증 테스트
     * UserService.phoneVerification 메서드 호출 시 전달되는 전화번호가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("휴대폰 인증코드 발송 엔드포인트 - 파라미터 검증 테스트")
    void phoneVerification_VerifyParameters() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");

        // when
        mockMvc.perform(post("/api/users/phone")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        verify(userService).phoneVerification("01012345678");
    }

    /**
     * 인증코드 확인 엔드포인트 - 성공 테스트
     * 유효한 인증코드로 요청 시 HTTP 200과 성공 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 확인 엔드포인트 - 성공 테스트")
    void verifyCode_Success() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("123456");

        when(userService.verifyCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode())).thenReturn(true);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()));

        // verify
        verify(userService).verifyCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode());
    }

    /**
     * 인증코드 확인 엔드포인트 - 실패 테스트
     * 유효하지 않은 인증코드로 요청 시 HTTP 400과 실패 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 확인 엔드포인트 - 실패 테스트")
    void verifyCode_Failure() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("654321"); // 잘못된 인증코드

        when(userService.verifyCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode())).thenReturn(false);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.CODE_VERIFICATION_FAIL.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.CODE_VERIFICATION_FAIL.getMessage()));

        // verify
        verify(userService).verifyCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode());
    }

    /**
     * 인증코드 확인 엔드포인트 - 파라미터 검증 테스트
     * UserService.verifyCode 메서드 호출 시 전달되는 매개변수가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("인증코드 확인 엔드포인트 - 파라미터 검증 테스트")
    void verifyCode_VerifyParameters() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("123456");

        when(userService.verifyCode(requestDto.getPhoneNumber(), requestDto.getVerificationCode())).thenReturn(true);

        // when
        mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        verify(userService).verifyCode("01012345678", "123456");
    }

    /**
     * PIN 번호 변경을 위한 인증 요청 엔드포인트 - 성공 테스트
     * 유효한 사용자 ID로 요청 시 HTTP 200과 성공 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증 요청 엔드포인트 - 성공 테스트")
    void requestPinVerification_Success() throws Exception {
        // given
        Long userId = 1L;

        // when
        ResultActions result = mockMvc.perform(post("/api/users/pin/" + userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()));

        // verify
        verify(userService).requestPinVerification(userId);
    }

    /**
     * PIN 번호 변경을 위한 인증 요청 엔드포인트 - 파라미터 검증 테스트
     * UserService.requestPinVerification 메서드 호출 시 전달되는 사용자 ID가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증 요청 엔드포인트 - 파라미터 검증 테스트")
    void requestPinVerification_VerifyParameters() throws Exception {
        // given
        Long userId = 1L;

        // when
        mockMvc.perform(post("/api/users/pin/" + userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        verify(userService).requestPinVerification(userId);
    }

    /**
     * PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 성공 테스트
     * 유효한 인증코드와 X-User-Id 헤더로 요청 시 HTTP 200과 성공 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 성공 테스트")
    void verifyCodeForPinChange_Success() throws Exception {
        // given
        String userId = "1";
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("123456");

        when(userService.verifyCodeForPinChange(requestDto.getPhoneNumber(), requestDto.getVerificationCode(), Long.parseLong(userId)))
                .thenReturn(true);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()));

        // verify
        verify(userService).verifyCodeForPinChange(requestDto.getPhoneNumber(), requestDto.getVerificationCode(), Long.parseLong(userId));
    }

    /**
     * PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 실패 테스트
     * 유효하지 않은 인증코드로 요청 시 HTTP 400과 실패 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 실패 테스트")
    void verifyCodeForPinChange_Failure() throws Exception {
        // given
        String userId = "1";
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("654321"); // 잘못된 인증코드

        when(userService.verifyCodeForPinChange(requestDto.getPhoneNumber(), requestDto.getVerificationCode(), Long.parseLong(userId)))
                .thenReturn(false);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.CODE_VERIFICATION_FAIL.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.CODE_VERIFICATION_FAIL.getMessage()));

        // verify
        verify(userService).verifyCodeForPinChange(requestDto.getPhoneNumber(), requestDto.getVerificationCode(), Long.parseLong(userId));
    }

    /**
     * PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 파라미터 검증 테스트
     * UserService.verifyCodeForPinChange 메서드 호출 시 전달되는 매개변수가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경을 위한 인증코드 확인 엔드포인트 - 파라미터 검증 테스트")
    void verifyCodeForPinChange_VerifyParameters() throws Exception {
        // given
        String userId = "1";
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01012345678");
        requestDto.setVerificationCode("123456");

        when(userService.verifyCodeForPinChange(requestDto.getPhoneNumber(), requestDto.getVerificationCode(), Long.parseLong(userId)))
                .thenReturn(true);

        // when
        mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        verify(userService).verifyCodeForPinChange("01012345678", "123456", 1L);
    }

    /**
     * PIN 번호 변경 엔드포인트 - 성공 테스트
     * 유효한 PinCodeRequestDto로 요청 시 HTTP 200과 성공 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경 엔드포인트 - 성공 테스트")
    void updatePinCode_Success() throws Exception {
        // given
        PinCodeRequestDto requestDto = new PinCodeRequestDto();
        requestDto.setUserId(1L);
        requestDto.setPinCode("123456");

        // when
        ResultActions result = mockMvc.perform(patch("/api/users/pin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()));

        // verify
        verify(userService).updatePinCode(requestDto.getUserId(), requestDto.getPinCode());
    }

    /**
     * PIN 번호 변경 엔드포인트 - 파라미터 검증 테스트
     * UserService.updatePinCode 메서드 호출 시 전달되는 매개변수가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("PIN 번호 변경 엔드포인트 - 파라미터 검증 테스트")
    void updatePinCode_VerifyParameters() throws Exception {
        // given
        PinCodeRequestDto requestDto = new PinCodeRequestDto();
        requestDto.setUserId(1L);
        requestDto.setPinCode("123456");

        // when
        mockMvc.perform(patch("/api/users/pin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        verify(userService).updatePinCode(1L, "123456");
    }

    /**
     * 사용자 정보 조회 엔드포인트 - 성공 테스트
     * 유효한 X-User-Id 헤더로 요청 시 HTTP 200과 올바른 BaseResponse<UserInfoResponseDto>가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 엔드포인트 - 성공 테스트")
    void getUserInfo_Success() throws Exception {
        // given
        String userId = "1";
        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .profileImage("https://example.com/profile.jpg")
                .build();

        when(userService.getUserInfo(Long.parseLong(userId))).thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .header("X-User-Id", userId))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.username").value("홍길동"))
                .andExpect(jsonPath("$.result.phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.result.profileImage").value("https://example.com/profile.jpg"));

        // verify
        verify(userService).getUserInfo(Long.parseLong(userId));
    }

    /**
     * 사용자 정보 조회 엔드포인트 - 파라미터 검증 테스트
     * UserService.getUserInfo 메서드 호출 시 전달되는 사용자 ID가 정확한지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 엔드포인트 - 파라미터 검증 테스트")
    void getUserInfo_VerifyParameters() throws Exception {
        // given
        String userId = "1";
        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .profileImage("https://example.com/profile.jpg")
                .build();

        when(userService.getUserInfo(Long.parseLong(userId))).thenReturn(responseDto);

        // when
        mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .header("X-User-Id", userId))
                .andDo(print());

        // then
        verify(userService).getUserInfo(1L);
    }

    /**
     * 사용자 정보 조회 엔드포인트 - 헤더 누락 테스트
     * X-User-Id 헤더가 없을 경우 적절한 오류 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 엔드포인트 - 헤더 누락 테스트")
    void getUserInfo_MissingHeader() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/users/info")
                .with(csrf()))
                .andDo(print());

        // then
        // 이 테스트는 컨트롤러가 헤더가 없을 때 어떻게 동작하는지에 따라 달라질 수 있습니다.
        // 일반적으로는 400 Bad Request 응답이 예상됩니다.
        result.andExpect(status().isBadRequest());
    }
}
