package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.request.UpdateAliasRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountOpenBankingService accountOpenBankingService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    // 1. 계좌 생성(정상/복구/인증실패)
    @Test
    @DisplayName("Redis 계좌 인증 & 기존 계좌 없으면 신규 생성")
    void createLinkedAccount_NewAccount_Success() {
        Long userId = 1L;
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .accountNumber("123-456")
                .bankCode(1)
                .accountTypeCode(1)
                .build();

        String redisKey = "account:lookup:" + userId;
        String lookupKey = "1:123-456:1";

        when(setOperations.isMember(redisKey, lookupKey)).thenReturn(true);
        when(accountRepository.findByAccountNumber("123-456")).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDto result = accountService.createLinkedAccount(userId, dto);

        assertThat(result.getAccountNumber()).isEqualTo("123-456");
        assertThat(result.getBankCode()).isEqualTo(1);
        assertThat(result.getAccountTypeCode()).isEqualTo("예금");

        verify(accountRepository).save(any());
    }

    @Test
    @DisplayName("계좌 인증 실패시 예외")
    void createLinkedAccount_InvalidInRedis_ShouldThrow() {
        Long userId = 1L;
        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .accountNumber("999-999")
                .bankCode(1)
                .accountTypeCode(1)
                .build();
        String redisKey = "account:lookup:" + userId;
        String lookupKey = "1:999-999:1";
        when(setOperations.isMember(redisKey, lookupKey)).thenReturn(false);

        assertThatThrownBy(() -> accountService.createLinkedAccount(userId, dto))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("본인 명의의 계좌만 연동할 수 있습니다.");

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("삭제된 계좌 복구")
    void createLinkedAccount_DeletedAccountRestore() {
        Long userId = 1L;
        String accountNumber = "333-888";
        int bankCode = 2;
        int accountTypeCode = 1;
        String redisKey = "account:lookup:" + userId;
        String lookupKey = "2:333-888:1";

        CreateAccountRequestDto dto = CreateAccountRequestDto.builder()
                .accountNumber(accountNumber)
                .bankCode(bankCode)
                .accountTypeCode(accountTypeCode)
                .build();

        LinkedAccount deleted = LinkedAccount.builder()
                .accountNumber(accountNumber)
                .bankCode(BankCode.fromIdx(bankCode))
                .accountTypeCode(AccountTypeCode.fromIdx(accountTypeCode))
                .userId(userId)
                .isDeleted(true)
                .build();

        when(setOperations.isMember(redisKey, lookupKey)).thenReturn(true);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(deleted));

        AccountResponseDto result = accountService.createLinkedAccount(userId, dto);

        assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(deleted.getIsDeleted()).isFalse(); // 복구
    }

    // 2. 전체 계좌/잔액 조회 (비동기)
    @Test
    @DisplayName("연동 계좌 없으면 예외")
    void findAllAccounts_Empty_ShouldThrow() {
        Long userId = 42L;
        when(accountRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(List.of());

        CompletableFuture<List<AccountBalanceResponseDto>> future = accountService.findAllAccounts(userId);

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(AccountException.class)
                .hasRootCauseMessage("요청하신 계좌가 존재하지 않습니다.");

        verify(accountRepository).findByUserIdAndIsDeletedFalse(userId);
    }

    @Test
    @DisplayName("계좌/잔액 병렬 조회 정상 반환")
    void findAllAccounts_Success() {
        Long userId = 2L;
        LinkedAccount acc1 = LinkedAccount.builder()
                .accountId(1L).accountNumber("1").bankCode(BankCode.KAKAO_BANK)
                .userId(userId).accountTypeCode(AccountTypeCode.DEPOSIT).build();

        LinkedAccount acc2 = LinkedAccount.builder()
                .accountId(2L).accountNumber("2").bankCode(BankCode.SSOK_BANK)
                .userId(userId).accountTypeCode(AccountTypeCode.SAVINGS).build();

        when(accountRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(List.of(acc1, acc2));

        OpenBankingAccountBalanceResponseDto openBankingDto1 =
                OpenBankingAccountBalanceResponseDto.builder().balance(10000L).build();
        OpenBankingAccountBalanceResponseDto openBankingDto2 =
                OpenBankingAccountBalanceResponseDto.builder().balance(5000L).build();

        when(accountOpenBankingService.fetchAccountBalanceFromOpenBanking(any(OpenBankingAccountBalanceRequestDto.class)))
                .thenReturn(CompletableFuture.completedFuture(openBankingDto1))
                .thenReturn(CompletableFuture.completedFuture(openBankingDto2));

        List<AccountBalanceResponseDto> result = accountService.findAllAccounts(userId).join();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBalance()).isEqualTo(10000L);
        assertThat(result.get(1).getBalance()).isEqualTo(5000L);
    }

    // 3. 단일 계좌 상세 조회 (비동기)
    @Test
    @DisplayName("계좌 ID 상세 조회 성공")
    void findAccountById_Success() {
        Long userId = 1L;
        Long accountId = 10L;
        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("333-3333-3333")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId))
                .thenReturn(Optional.of(acc));

        // OpenBankingAccountBalanceResponseDto mock 생성
        OpenBankingAccountBalanceResponseDto openBankingDto =
                OpenBankingAccountBalanceResponseDto.builder()
                        .balance(1000L)
                        .build();

        when(accountOpenBankingService.fetchAccountBalanceFromOpenBanking(any()))
                .thenReturn(CompletableFuture.completedFuture(openBankingDto));

        AccountBalanceResponseDto result = accountService.findAccountById(userId, accountId).join();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getBalance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("계좌 ID 조회 실패시 예외")
    void findAccountById_NotFound() {
        Long userId = 2L;
        Long accountId = 9L;
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> accountService.findAccountById(userId, accountId));
        assertThat(thrown).isInstanceOf(AccountException.class)
                .hasMessage("요청하신 계좌가 존재하지 않습니다.");
    }

    // 4. 계좌 삭제
    @Test
    @DisplayName("연동 계좌 삭제 성공")
    void deleteLinkedAccount_Success() {
        Long userId = 1L;
        Long accountId = 10L;
        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("333-3333-3333")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .isPrimaryAccount(false)
                .isDeleted(false)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(acc));
        AccountResponseDto result = accountService.deleteLinkedAccount(userId, accountId);

        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAccountNumber()).isEqualTo(acc.getAccountNumber());
        assertThat(acc.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("삭제 계좌 미존재시 예외")
    void deleteLinkedAccount_NotFound() {
        Long userId = 1L;
        Long accountId = 10L;
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).delete(any());
    }

    @Test
    @DisplayName("주계좌 삭제 시도 시 예외")
    void deleteLinkedAccount_Primary_ShouldThrow() {
        Long userId = 1L;
        Long accountId = 77L;
        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .userId(userId)
                .accountNumber("333-3333-7777")
                .isPrimaryAccount(true)
                .isDeleted(false)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.deleteLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("주계좌는 삭제할 수 없습니다.");
    }

    // 5. 별칭 수정
    @Test
    @DisplayName("계좌 별칭 정상 수정")
    void updateLinkedAccountAlias_Success() {
        Long userId = 1L;
        Long accountId = 100L;
        String newAlias = "비상금 통장";

        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("123-123")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId))
                .thenReturn(Optional.of(acc));

        var dto = new UpdateAliasRequestDto(newAlias);
        AccountResponseDto result = accountService.updateLinkedAccountAlias(userId, accountId, dto);

        assertThat(result.getAccountAlias()).isEqualTo(newAlias);
        assertThat(acc.getAccountAlias()).isEqualTo(newAlias);
    }

    @Test
    @DisplayName("별칭 수정 대상 계좌 없음")
    void updateLinkedAccountAlias_NotFound() {
        Long userId = 1L;
        Long accountId = 101L;
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateLinkedAccountAlias(userId, accountId, new UpdateAliasRequestDto("새 별칭")))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("별칭 공백/유효성 위반")
    void updateLinkedAccountAlias_Blank_ShouldThrow() {
        Long userId = 1L;
        Long accountId = 102L;
        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .userId(userId)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.updateLinkedAccountAlias(userId, accountId, new UpdateAliasRequestDto(" ")))
                .isInstanceOf(AccountException.class)
                .hasMessageContaining("계좌 별칭이 올바른 형식이 아닙니다.");
    }

    // 6. 주계좌 변경
    @Test
    @DisplayName("주계좌 변경 정상")
    void updatePrimaryLinkedAccount_Success() {
        Long userId = 1L;
        Long currentPrimaryId = 10L;
        Long newPrimaryId = 20L;

        LinkedAccount currentPrimary = LinkedAccount.builder()
                .accountId(currentPrimaryId)
                .isPrimaryAccount(true)
                .userId(userId)
                .accountNumber("123")
                .bankCode(BankCode.KAKAO_BANK)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        LinkedAccount newPrimary = LinkedAccount.builder()
                .accountId(newPrimaryId)
                .isPrimaryAccount(false)
                .userId(userId)
                .accountNumber("456")
                .bankCode(BankCode.KAKAO_BANK)
                .accountTypeCode(AccountTypeCode.SAVINGS)
                .build();

        when(accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId)).thenReturn(Optional.of(currentPrimary));
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(newPrimaryId, userId)).thenReturn(Optional.of(newPrimary));

        AccountResponseDto result = accountService.updatePrimaryLinkedAccount(userId, newPrimaryId);

        assertThat(result.getAccountId()).isEqualTo(newPrimaryId);
        assertThat(currentPrimary.getIsPrimaryAccount()).isFalse();
        assertThat(newPrimary.getIsPrimaryAccount()).isTrue();

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 주계좌인 경우 예외")
    void updatePrimaryLinkedAccount_AlreadyPrimary() {
        Long userId = 1L;
        Long accountId = 10L;
        LinkedAccount acc = LinkedAccount.builder()
                .accountId(accountId)
                .isPrimaryAccount(true)
                .userId(userId)
                .build();

        when(accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId)).thenReturn(Optional.of(acc));
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.updatePrimaryLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("주계좌 변경 대상 없음 예외")
    void updatePrimaryLinkedAccount_TargetNotFound() {
        Long userId = 1L;
        Long accountId = 100L;

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updatePrimaryLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

}