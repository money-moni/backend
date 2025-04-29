package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    public AccountServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 연동 계좌를 생성할 수 있다")
    void createLinkedAccount_Success() {
        // given
        Long userId = 1L;
        CreateAccountRequestDto requestDto = CreateAccountRequestDto.builder()
                .accountNumber("123-456-789")
                .bankCode(1L)
                .accountTypeCode(1L)
                .build();

        when(accountRepository.existsByAccountNumber(requestDto.getAccountNumber())).thenReturn(false);
        when(accountRepository.save(any(LinkedAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountResponseDto responseDto = accountService.createLinkedAccount(userId, requestDto);

        // then
        assertThat(responseDto.getAccountNumber()).isEqualTo(requestDto.getAccountNumber());
        assertThat(responseDto.getBankCode()).isEqualTo(1);
        assertThat(responseDto.getAccountTypeCode()).isEqualTo("예금");

        ArgumentCaptor<LinkedAccount> captor = ArgumentCaptor.forClass(LinkedAccount.class);
        verify(accountRepository).save(captor.capture());

        LinkedAccount savedAccount = captor.getValue();
        assertThat(savedAccount.getAccountNumber()).isEqualTo(requestDto.getAccountNumber());
        assertThat(savedAccount.getBankCode()).isEqualTo(BankCode.fromIdx(requestDto.getBankCode()));
        assertThat(savedAccount.getAccountTypeCode()).isEqualTo(AccountTypeCode.fromIdx(requestDto.getAccountTypeCode()));
        assertThat(savedAccount.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("이미 존재하는 계좌번호로 생성하려 하면 예외가 발생한다")
    void createLinkedAccount_AlreadyExists() {
        // given
        Long userId = 1L;
        CreateAccountRequestDto requestDto = CreateAccountRequestDto.builder()
                .accountNumber("123-456-789")
                .bankCode(1L)
                .accountTypeCode(1L)
                .build();

        when(accountRepository.existsByAccountNumber(requestDto.getAccountNumber())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> accountService.createLinkedAccount(userId, requestDto))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, times(1)).existsByAccountNumber(requestDto.getAccountNumber());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자의 모든 연동 계좌를 조회할 수 있다")
    void findAllAccounts_Success() {
        // given
        Long userId = 1L;
        LinkedAccount account1 = LinkedAccount.builder()
                .accountId(1L)
                .accountNumber("111-1111-1111")
                .bankCode(BankCode.SSOK_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();
        LinkedAccount account2 = LinkedAccount.builder()
                .accountId(2L)
                .accountNumber("222-2222-2222")
                .bankCode(BankCode.KAKAO_BANK)
                .userId(userId)
                .accountTypeCode(AccountTypeCode.SAVINGS)
                .build();

        when(accountRepository.findByUserId(userId)).thenReturn(List.of(account1, account2));

        // when
        var result = accountService.findAllAccounts(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAccountId()).isEqualTo(account1.getAccountId());
        assertThat(result.get(1).getAccountNumber()).isEqualTo(account2.getAccountNumber());

        verify(accountRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자의 모든 연동 계좌가 없으면 예외가 발생한다")
    void findAllAccounts_NotFound() {
        // given
        Long userId = 1L;
        when(accountRepository.findByUserId(userId)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> accountService.findAllAccounts(userId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository).findByUserId(userId);
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

        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        // when
        var result = accountService.findAccountById(userId, accountId);

        // then
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAccountNumber()).isEqualTo(account.getAccountNumber());

        verify(accountRepository).findByAccountIdAndUserId(accountId, userId);
    }

    @Test
    @DisplayName("계좌 ID로 상세 조회 시 계좌를 찾을 수 없으면 예외가 발생한다")
    void findAccountById_NotFound() {
        // given
        Long userId = 1L;
        Long accountId = 10L;

        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.findAccountById(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository).findByAccountIdAndUserId(accountId, userId);
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

        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        // when
        AccountResponseDto result = accountService.deleteLinkedAccount(userId, accountId);

        // then
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAccountNumber()).isEqualTo(account.getAccountNumber());

        ArgumentCaptor<LinkedAccount> captor = ArgumentCaptor.forClass(LinkedAccount.class);
        verify(accountRepository).delete(captor.capture());

        LinkedAccount deletedAccount = captor.getValue();
        assertThat(deletedAccount.getAccountId()).isEqualTo(accountId);
    }

    @Test
    @DisplayName("삭제할 계좌를 찾을 수 없으면 예외가 발생한다")
    void deleteLinkedAccount_NotFound() {
        // given
        Long userId = 1L;
        Long accountId = 10L;

        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.deleteLinkedAccount(userId, accountId))
                .isInstanceOf(AccountException.class);

        verify(accountRepository, never()).delete(any());
    }
}