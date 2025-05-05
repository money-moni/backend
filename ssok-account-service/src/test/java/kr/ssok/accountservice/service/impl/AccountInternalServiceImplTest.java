package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class AccountInternalServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountInternalServiceImpl accountInternalService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자 ID와 계좌 ID로 계좌 정보를 조회할 수 있다")
    void findAccountByUserIdAndAccountId_Success() {
        Long userId = 1L;
        Long accountId = 100L;
        LinkedAccount account = LinkedAccount.builder()
                .accountId(accountId)
                .userId(userId)
                .accountNumber("111-1111")
                .bankCode(BankCode.SSOK_BANK)
                .accountTypeCode(AccountTypeCode.DEPOSIT)
                .build();

        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        AccountInfoResponseDto result = accountInternalService.findAccountByUserIdAndAccountId(userId, accountId);

        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAccountNumber()).isEqualTo("111-1111");
    }

    @Test
    @DisplayName("계좌 정보 조회 시 계좌가 없으면 예외가 발생한다")
    void findAccountByUserIdAndAccountId_NotFound() {
        Long userId = 1L;
        Long accountId = 100L;
        when(accountRepository.findByAccountIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountInternalService.findAccountByUserIdAndAccountId(userId, accountId))
                .isInstanceOf(AccountException.class);
    }

    @Test
    @DisplayName("계좌번호로 계좌 ID를 조회할 수 있다")
    void findAccountIdByAccountNumber_Success() {
        LinkedAccount account = LinkedAccount.builder()
                .accountId(200L)
                .accountNumber("222-2222")
                .build();

        when(accountRepository.findByAccountNumber("222-2222")).thenReturn(Optional.of(account));

        AccountIdResponseDto result = accountInternalService.findAccountIdByAccountNumber("222-2222");

        assertThat(result.getAccountId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("계좌번호로 계좌 ID 조회 시 존재하지 않으면 예외가 발생한다")
    void findAccountIdByAccountNumber_NotFound() {
        when(accountRepository.findByAccountNumber("not-found")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountInternalService.findAccountIdByAccountNumber("not-found"))
                .isInstanceOf(AccountException.class);
    }

    @Test
    @DisplayName("사용자의 모든 계좌 ID를 조회할 수 있다")
    void findAllAccountIds_Success() {
        Long userId = 1L;
        LinkedAccount acc1 = LinkedAccount.builder().accountId(1L).userId(userId).build();
        LinkedAccount acc2 = LinkedAccount.builder().accountId(2L).userId(userId).build();

        when(accountRepository.findByUserId(userId)).thenReturn(List.of(acc1, acc2));

        List<AccountIdResponseDto> result = accountInternalService.findAllAccountIds(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAccountId()).isEqualTo(1L);
        assertThat(result.get(1).getAccountId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자의 계좌 ID 조회 시 아무 계좌도 없으면 예외가 발생한다")
    void findAllAccountIds_Empty() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> accountInternalService.findAllAccountIds(1L))
                .isInstanceOf(AccountException.class);
    }
}