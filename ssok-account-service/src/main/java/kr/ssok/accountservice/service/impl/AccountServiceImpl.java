package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.AccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;


    @Override
    public AccountResponseDto createLinkedAccount(Long userId, AccountRequestDto accountRequestDto) {
        if (this.accountRepository.existsByAccountNumber(accountRequestDto.getAccountNumber())) {
            log.warn("[POST] Account {} already exists", accountRequestDto.getAccountNumber());
        }
        LinkedAccount linkedAccount = LinkedAccount.builder()
                .accountNumber(accountRequestDto.getAccountNumber())
                .bankCode(BankCode.fromIdx(accountRequestDto.getBankCode()))
                .userId(userId)
                .accountTypeCode(AccountTypeCode.fromIdx(accountRequestDto.getAccountTypeCode()))
                .build();

        this.accountRepository.save(linkedAccount);

        return linkedAccount.toAccountResponseDto();
    }


}
