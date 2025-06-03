package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.client.OpenBankingClient;
import kr.ssok.accountservice.client.UserServiceClient;
import kr.ssok.accountservice.client.dto.response.OpenBankingResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountOpenBankingServiceImplTest {
    @Mock
    private OpenBankingClient openBankingClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private AccountOpenBankingServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("정상적으로 전체 계좌 목록을 조회할 수 있다")
    void fetchAllAccountsFromOpenBanking_Success() throws Exception {
        Long userId = 1L;

        UserInfoResponseDto userInfo = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .build();

        OpenBankingAllAccountsResponseDto account1 = new OpenBankingAllAccountsResponseDto(1, "1111", 1);
        OpenBankingAllAccountsResponseDto account2 = new OpenBankingAllAccountsResponseDto(2, "2222", 2);

        when(userServiceClient.sendUserInfoRequest(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 200, "성공", userInfo));

        when(openBankingClient.sendAllAccountsRequest(any(OpenBankingAllAccountsRequestDto.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new OpenBankingResponse<>(true, "200", "성공", List.of(account1, account2))
                ));

        CompletableFuture<List<AllAccountsResponseDto>> future = service.fetchAllAccountsFromOpenBanking(userId);
        List<AllAccountsResponseDto> result = future.get();

        assertThat(result).hasSize(2);
        verify(userServiceClient).sendUserInfoRequest(userId.toString());
        verify(openBankingClient).sendAllAccountsRequest(any(OpenBankingAllAccountsRequestDto.class));
    }

    @Test
    @DisplayName("사용자 정보를 조회하지 못하면 예외가 발생한다")
    void fetchAllAccountsFromOpenBanking_UserInfoNotFound() {
        Long userId = 1L;

        when(userServiceClient.sendUserInfoRequest(userId.toString()))
                .thenReturn(null);

        // 동기 예외 발생이므로 future.get()이 아니라 바로 try-catch/catchThrowable
        Throwable thrown = catchThrowable(() -> service.fetchAllAccountsFromOpenBanking(userId));
        assertThat(thrown)
                .isInstanceOf(AccountException.class)
                .hasMessage("사용자 정보를 찾을 수 없습니다.");

        verify(userServiceClient).sendUserInfoRequest(userId.toString());
        verify(openBankingClient, never()).sendAllAccountsRequest(any());
    }

    @Test
    @DisplayName("오픈뱅킹 서버에서 계좌 정보를 조회하지 못하면 예외가 발생한다")
    void fetchAllAccountsFromOpenBanking_OpenBankingFail() throws Exception {
        Long userId = 1L;
        UserInfoResponseDto userInfo = UserInfoResponseDto.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .build();

        when(userServiceClient.sendUserInfoRequest(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 200, "성공", userInfo));

        // 오픈뱅킹 서버 응답이 실패(null)
        when(openBankingClient.sendAllAccountsRequest(any(OpenBankingAllAccountsRequestDto.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<List<AllAccountsResponseDto>> future = service.fetchAllAccountsFromOpenBanking(userId);

        assertThatThrownBy(future::get)
                .isInstanceOf(Exception.class) // ExecutionException
                .hasRootCauseInstanceOf(AccountException.class)
                .hasRootCauseMessage("오픈뱅킹 계좌 목록 조회에 실패했습니다.");
    }

    @Test
    @DisplayName("정상적으로 실명 조회를 수행할 수 있다")
    void fetchAccountOwnerFromOpenBanking_Success() throws Exception {
        AccountOwnerRequestDto requestDto = new AccountOwnerRequestDto("1234567890", 1);
        OpenBankingAccountOwnerResponseDto responseDto = new OpenBankingAccountOwnerResponseDto("홍길동");

        when(openBankingClient.sendAccountOwnerRequest(any(OpenBankingAccountOwnerRequestDto.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new OpenBankingResponse<>(true, "200", "성공", responseDto)
                ));

        CompletableFuture<AccountOwnerResponseDto> future = service.fetchAccountOwnerFromOpenBanking(requestDto);
        AccountOwnerResponseDto result = future.get();

        assertThat(result.getUsername()).isEqualTo("홍길동");
        assertThat(result.getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("실명 조회 실패 시 예외가 발생한다")
    void fetchAccountOwnerFromOpenBanking_Fail() {
        AccountOwnerRequestDto requestDto = new AccountOwnerRequestDto("1234567890", 1);

        when(openBankingClient.sendAccountOwnerRequest(any(OpenBankingAccountOwnerRequestDto.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<AccountOwnerResponseDto> future = service.fetchAccountOwnerFromOpenBanking(requestDto);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(CompletionException.class)
                .hasRootCauseInstanceOf(AccountException.class)
                .hasRootCauseMessage("오픈뱅킹 실명 조회에 실패했습니다.");
    }

    @Test
    @DisplayName("정상적으로 계좌 잔액을 조회할 수 있다")
    void fetchAccountBalanceFromOpenBanking_Success() throws Exception {
        OpenBankingAccountBalanceRequestDto requestDto = new OpenBankingAccountBalanceRequestDto("1234567890", 1);
        OpenBankingAccountBalanceResponseDto responseDto = new OpenBankingAccountBalanceResponseDto(50000L);

        when(openBankingClient.sendAccountBalanceRequest(eq(requestDto)))
                .thenReturn(CompletableFuture.completedFuture(
                        new OpenBankingResponse<>(true, "200", "성공", responseDto)
                ));

        CompletableFuture<OpenBankingAccountBalanceResponseDto> future = service.fetchAccountBalanceFromOpenBanking(requestDto);
        OpenBankingAccountBalanceResponseDto result = future.get();

        assertThat(result.getBalance()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("계좌 잔액 조회 실패 시 예외가 발생한다")
    void fetchAccountBalanceFromOpenBanking_Fail() {
        OpenBankingAccountBalanceRequestDto requestDto = new OpenBankingAccountBalanceRequestDto("1234567890", 1);

        when(openBankingClient.sendAccountBalanceRequest(eq(requestDto)))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<OpenBankingAccountBalanceResponseDto> future = service.fetchAccountBalanceFromOpenBanking(requestDto);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(CompletionException.class)
                .hasRootCauseInstanceOf(AccountException.class)
                .hasRootCauseMessage("오픈뱅킹 계좌 잔액 조회에 실패했습니다.");
    }
}