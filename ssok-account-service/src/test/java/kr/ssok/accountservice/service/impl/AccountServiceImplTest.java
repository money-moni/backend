package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.request.UpdateAliasRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.repository.AccountRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // redisTemplate.opsForSet() -> setOperations mock 리턴
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Redis에 유효성 인증된 계좌가 있고, 기존 계좌가 없으면 계좌를 새로 생성한다")
    void createLinkedAccount_WhenValidInRedisAndNoExistingAccount_ShouldCreateNew() {
        Long userId = 1L;
        CreateAccountRequestDto requestDto = CreateAccountRequestDto.builder()
                .accountNumber("123-456")
                .bankCode(1)
                .accountTypeCode(1)
                .build();

        String redisKey = "account:lookup:" + userId;
        String lookupKey = "1:123-456:1";

        when(redisTemplate.opsForSet().isMember(redisKey, lookupKey)).thenReturn(true);
        when(accountRepository.findByAccountNumber(requestDto.getAccountNumber())).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto result = accountService.createLinkedAccount(userId, requestDto);

        assertThat(result.getAccountNumber()).isEqualTo("123-456");
        assertThat(result.getBankCode()).isEqualTo(1);
        assertThat(result.getAccountTypeCode()).isEqualTo("예금");

        verify(redisTemplate, atLeastOnce()).opsForSet();
        verify(accountRepository).save(any());
    }


    @Test
    @DisplayName("Redis에 계좌 인증 정보가 없으면 예외가 발생한다")
    void createLinkedAccount_WhenRedisValidationFails_ShouldThrow() {
        Long userId = 1L;
        CreateAccountRequestDto requestDto = CreateAccountRequestDto.builder()
                .accountNumber("123-456")
                .bankCode(1)
                .accountTypeCode(1)
                .build();

        String redisKey = "account:lookup:" + userId;
        String lookupKey = "1:123-456:1";

        when(redisTemplate.opsForSet().isMember(redisKey, lookupKey)).thenReturn(false);

        assertThatThrownBy(() -> accountService.createLinkedAccount(userId, requestDto))
                .isInstanceOf(AccountException.class)
                .hasMessage("본인 명의의 계좌만 연동할 수 있습니다.");

        verify(accountRepository, never()).save(any());
    }


    @Test
    @DisplayName("Redis에 인증된 계좌이고, 기존 삭제된 계좌가 있으면 복구 처리한다")
    void createLinkedAccount_WhenExistingAccountIsDeleted_ShouldRestore() {
        // given
        Long userId = 1L;
        String accountNumber = "123-456";
        int bankCode = 1;
        int typeCode = 2;
        String redisKey = "account:lookup:" + userId;
        String lookupKey = bankCode + ":" + accountNumber + ":" + typeCode;

        CreateAccountRequestDto requestDto = CreateAccountRequestDto.builder()
                .accountNumber(accountNumber)
                .bankCode(bankCode)
                .accountTypeCode(typeCode)
                .build();

        // Redis key 인증 통과
        when(setOperations.isMember(redisKey, lookupKey)).thenReturn(true);

        LinkedAccount deletedAccount = LinkedAccount.builder()
                .accountNumber(accountNumber)
                .bankCode(BankCode.fromIdx(bankCode))
                .accountTypeCode(AccountTypeCode.fromIdx(typeCode))
                .userId(userId)
                .isDeleted(true)
                .build();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(deletedAccount));

        // when
        AccountResponseDto result = accountService.createLinkedAccount(userId, requestDto);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(deletedAccount.getIsDeleted()).isFalse(); // 복구 여부 확인
    }


    @Test
    @DisplayName("사용자의 모든 연동 계좌가 없으면 예외가 발생한다")
    void findAllAccounts_NotFound() {
        // given
        Long userId = 1L;
        when(accountRepository.findByUserIdAndIsDeletedFalse(userId)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> accountService.findAllAccounts(userId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository).findByUserIdAndIsDeletedFalse(userId);
    }

    @Test
    @DisplayName("계좌 ID로 상세 조회할 수 있다")
    void findAccountById_Success() {
        // given
        Long userId = 1L;
        Long accountId = 10L;
        LinkedAccount account = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("333-3333-3333")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(account));

        // when
        var result = accountService.findAccountById(userId, accountId);

        // then
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAccountNumber()).isEqualTo(account.getAccountNumber());

        verify(accountRepository).findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId);
    }

    @Test
    @DisplayName("계좌 ID로 상세 조회 시 계좌를 찾을 수 없으면 예외가 발생한다")
    void findAccountById_NotFound() {
        // given
        Long userId = 1L;
        Long accountId = 10L;

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.findAccountById(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository).findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId);
    }

    @Test
    @DisplayName("정상적으로 연동 계좌를 삭제할 수 있다")
    void deleteLinkedAccount_Success() {
        // given
        Long userId = 1L;
        Long accountId = 10L;
        LinkedAccount account = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("333-3333-3333")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(account));

        // when
        AccountResponseDto result = accountService.deleteLinkedAccount(userId, accountId);

        // then
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(account.getIsDeleted()).isTrue(); // dirty checking 결과 검증

    }

    @Test
    @DisplayName("삭제할 계좌를 찾을 수 없으면 예외가 발생한다")
    void deleteLinkedAccount_NotFound() {
        // given
        Long userId = 1L;
        Long accountId = 10L;

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.deleteLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).delete(any());
    }

    @Test
    @DisplayName("계좌 별칭을 정상적으로 수정할 수 있다")
    void updateAccountAlias_Success() {
        Long userId = 1L;
        Long accountId = 100L;
        String newAlias = "비상금 통장";

        LinkedAccount account = LinkedAccount.builder()
                .accountId(accountId)
                .accountNumber("123-123")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId))
                .thenReturn(Optional.of(account));

        var dto = new UpdateAliasRequestDto(newAlias);
        AccountResponseDto result = accountService.updateLinkedAccountAlias(userId, accountId, dto);

        assertThat(result.getAccountAlias()).isEqualTo(newAlias);
        assertThat(account.getAccountAlias()).isEqualTo(newAlias);
    }


    @Test
    @DisplayName("계좌 별칭 수정 시 대상 계좌가 없으면 예외가 발생한다")
    void updateAccountAlias_NotFound() {
        Long userId = 1L;
        Long accountId = 100L;

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateLinkedAccountAlias(userId, accountId, new UpdateAliasRequestDto("새 별칭")))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상적으로 주계좌를 변경할 수 있다")
    void updatePrimaryAccount_Success() {
        // given
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

        // when
        AccountResponseDto result = accountService.updatePrimaryLinkedAccount(userId, newPrimaryId);

        // then
        assertThat(result.getAccountId()).isEqualTo(newPrimaryId);
        assertThat(currentPrimary.getIsPrimaryAccount()).isFalse();
        assertThat(newPrimary.getIsPrimaryAccount()).isTrue();

        // save 호출 검증은 제거함 (Dirty Checking 전략)
        verify(accountRepository, never()).save(any());
    }


    @Test
    @DisplayName("주계좌로 설정하려는 계좌가 이미 주계좌일 경우 예외가 발생한다")
    void updatePrimaryAccount_AlreadyPrimary() {
        Long userId = 1L;
        Long accountId = 10L;

        LinkedAccount account = LinkedAccount.builder()
                .accountId(accountId)
                .isPrimaryAccount(true)
                .userId(userId)
                .accountNumber("123")
                .bankCode(BankCode.SSOK_BANK)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updatePrimaryLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("주계좌 변경 시 대상 계좌가 존재하지 않으면 예외가 발생한다")
    void updatePrimaryAccount_TargetNotFound() {
        Long userId = 1L;
        Long accountId = 100L;

        when(accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updatePrimaryLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).save(any());
    }

}