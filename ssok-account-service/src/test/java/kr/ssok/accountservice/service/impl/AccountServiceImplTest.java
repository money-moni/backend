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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
                .bankCode(1L) // 예시로 SSOK_BANK라고 가정
                .accountTypeCode(1L) // 예시
                .build();

        when(accountRepository.existsByAccountNumber(requestDto.getAccountNumber())).thenReturn(false);
        when(accountRepository.save(any(LinkedAccount.class))).thenAnswer(invocation -> {
            LinkedAccount savedAccount = invocation.getArgument(0);
            return savedAccount;
        });

        // when
        AccountResponseDto responseDto = accountService.createLinkedAccount(userId, requestDto);

        // then
        assertThat(responseDto.getAccountNumber()).isEqualTo(requestDto.getAccountNumber());
        assertThat(responseDto.getBankCode()).isEqualTo(BankCode.fromIdx(requestDto.getBankCode()).getIdx());
        assertThat(responseDto.getAccountTypeCode()).isEqualTo(AccountTypeCode.fromIdx(requestDto.getAccountTypeCode()));

        verify(accountRepository, times(1)).existsByAccountNumber(requestDto.getAccountNumber());
        verify(accountRepository, times(1)).save(any(LinkedAccount.class));
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
        verify(accountRepository, never()).save(any(LinkedAccount.class));
    }
}