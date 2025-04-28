package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 계좌 서비스 비즈니스 로직을 구현한 클래스
 *
 * <p>계좌 생성, 조회, 수정, 삭제 등 LinkedAccount 관련 기능을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;


    /**
     * 사용자의 연동 계좌를 생성합니다.
     *
     * <p>이미 동일한 계좌 번호가 존재하는 경우 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @param createAccountRequestDto 계좌 생성 요청 데이터
     * @return 생성된 계좌 정보를 담은 응답 DTO
     * @throws AccountException 이미 동일한 계좌가 존재하는 경우 발생
     */
    @Override
    public AccountResponseDto createLinkedAccount(Long userId, CreateAccountRequestDto createAccountRequestDto) {
        if (this.accountRepository.existsByAccountNumber(createAccountRequestDto.getAccountNumber())) {
            log.warn("[POST] Account {} already exists", createAccountRequestDto.getAccountNumber());
            throw new AccountException(AccountResponseStatus.ACCOUNT_ALREADY_EXISTS);
        }
        LinkedAccount linkedAccount = LinkedAccount.builder()
                .accountNumber(createAccountRequestDto.getAccountNumber())
                .bankCode(BankCode.fromIdx(createAccountRequestDto.getBankCode()))
                .userId(userId)
                .accountTypeCode(AccountTypeCode.fromIdx(createAccountRequestDto.getAccountTypeCode()))
                .build();

        this.accountRepository.save(linkedAccount);

        return linkedAccount.toAccountResponseDto();
    }


}
