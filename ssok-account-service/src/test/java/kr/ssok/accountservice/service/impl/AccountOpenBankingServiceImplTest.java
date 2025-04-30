package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.client.OpenBankingClient;
import kr.ssok.accountservice.client.UserServiceClient;
import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.common.exception.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountOpenBankingServiceImplTest {
    @Mock
    private OpenBankingClient openBankingClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AccountOpenBankingServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 전체 계좌 목록을 조회할 수 있다")
    void fetchAllAccountsFromOpenBanking_Success() {
        Long userId = 1L;

        UserInfoResponseDto userInfo = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .build();

        OpenBankingAllAccountsResponseDto account1 = new OpenBankingAllAccountsResponseDto(1, "1111", 1);
        OpenBankingAllAccountsResponseDto account2 = new OpenBankingAllAccountsResponseDto(2, "2222", 2);

        when(userServiceClient.sendUserInfoRequest(userId))
                .thenReturn(new BaseResponse<>(true, 200, "성공", userInfo));

        when(openBankingClient.sendAllAccountsRequest(any(OpenBankingAllAccountsRequestDto.class)))
                .thenReturn(new BaseResponse<>(true, 200, "성공", List.of(account1, account2)));

        List<AllAccountsResponseDto> result = service.fetchAllAccountsFromOpenBanking(userId);

        assertThat(result).hasSize(2);
        verify(userServiceClient).sendUserInfoRequest(userId);
        verify(openBankingClient).sendAllAccountsRequest(any());
    }

    @Test
    @DisplayName("사용자 정보를 조회하지 못하면 예외가 발생한다")
    void fetchAllAccountsFromOpenBanking_UserInfoNotFound() {
        Long userId = 1L;

        when(userServiceClient.sendUserInfoRequest(userId))
                .thenReturn(null);

        assertThatThrownBy(() -> service.fetchAllAccountsFromOpenBanking(userId))
                .isInstanceOf(AccountException.class);

        verify(userServiceClient).sendUserInfoRequest(userId);
        verify(openBankingClient, never()).sendAllAccountsRequest(any());
    }

    @Test
    @DisplayName("오픈뱅킹 서버에서 계좌 정보를 조회하지 못하면 예외가 발생한다")
    void fetchAllAccountsFromOpenBanking_OpenBankingFail() {
        Long userId = 1L;
        UserInfoResponseDto userInfo = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .build();

        when(userServiceClient.sendUserInfoRequest(userId))
                .thenReturn(new BaseResponse<>(true, 200, "성공", userInfo));

        when(openBankingClient.sendAllAccountsRequest(any())).thenReturn(null);

        assertThatThrownBy(() -> service.fetchAllAccountsFromOpenBanking(userId))
                .isInstanceOf(AccountException.class);
    }

    @Test
    @DisplayName("정상적으로 실명 조회를 수행할 수 있다")
    void fetchAccountOwnerFromOpenBanking_Success() {
        AccountOwnerRequestDto requestDto = new AccountOwnerRequestDto("1234567890", 1);
        OpenBankingAccountOwnerResponseDto responseDto = new OpenBankingAccountOwnerResponseDto("홍길동");

        when(openBankingClient.sendAccountOwnerRequest(any(OpenBankingAccountOwnerRequestDto.class)))
                .thenReturn(new BaseResponse<>(true, 200, "성공", responseDto));

        AccountOwnerResponseDto result = service.fetchAccountOwnerFromOpenBanking(requestDto);

        assertThat(result.getUsername()).isEqualTo("홍길동");
        assertThat(result.getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("실명 조회 실패 시 예외가 발생한다")
    void fetchAccountOwnerFromOpenBanking_Fail() {
        AccountOwnerRequestDto requestDto = new AccountOwnerRequestDto("1234567890", 1);

        when(openBankingClient.sendAccountOwnerRequest(any())).thenReturn(null);

        assertThatThrownBy(() -> service.fetchAccountOwnerFromOpenBanking(requestDto))
                .isInstanceOf(AccountException.class);
    }

    @Test
    @DisplayName("정상적으로 계좌 잔액을 조회할 수 있다")
    void fetchAccountBalanceFromOpenBanking_Success() {
        OpenBankingAccountBalanceRequestDto requestDto = new OpenBankingAccountBalanceRequestDto("1234567890", 1);
        OpenBankingAccountBalanceResponseDto responseDto = new OpenBankingAccountBalanceResponseDto(50000L);

        when(openBankingClient.sendAccountBalanceRequest(requestDto))
                .thenReturn(new BaseResponse<>(true, 200, "성공", responseDto));

        OpenBankingAccountBalanceResponseDto result = service.fetchAccountBalanceFromOpenBanking(requestDto);

        assertThat(result.getBalance()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("계좌 잔액 조회 실패 시 예외가 발생한다")
    void fetchAccountBalanceFromOpenBanking_Fail() {
        OpenBankingAccountBalanceRequestDto requestDto = new OpenBankingAccountBalanceRequestDto("1234567890", 1);

        when(openBankingClient.sendAccountBalanceRequest(requestDto)).thenReturn(null);

        assertThatThrownBy(() -> service.fetchAccountBalanceFromOpenBanking(requestDto))
                .isInstanceOf(AccountException.class);
    }
}