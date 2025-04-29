package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
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

import java.util.List;
import java.util.stream.Collectors;

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

        return AccountResponseDto.from(linkedAccount);
    }

    /**
     * 사용자 ID에 해당하는 모든 연동 계좌 목록을 조회합니다.
     *
     * <p>등록된 연동 계좌가 없는 경우 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @return 연동 계좌 정보를 담은 {@link List}<{@link AccountBalanceResponseDto}>
     * @throws AccountException 사용자의 연동 계좌가 하나도 없는 경우 발생
     */
    @Override
    public List<AccountBalanceResponseDto> findAllAccounts(Long userId) {
        List<LinkedAccount> accounts = this.accountRepository.findByUserId(userId);

        if (accounts.isEmpty()) {
            log.warn("[GET] Account not found: userId={}", userId);
            throw new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
        }

        // TODO. 추후, 으픈뱅킹 API 호출을 통해 balance 값에 실제 값을 대입
        return accounts.stream()
                .map(account -> AccountBalanceResponseDto.from(9999999L, account))
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 상세 조회합니다.
     *
     * <p>해당하는 계좌가 존재하지 않는 경우 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 연동 계좌 정보를 담은 {@link AccountBalanceResponseDto}
     * @throws AccountException 계좌를 찾을 수 없는 경우 발생
     */
    @Override
    public AccountBalanceResponseDto findAccountById(Long userId, Long accountId) {
        LinkedAccount account = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        // TODO. 추후, 으픈뱅킹 API 호출을 통해 balance 값에 실제 값을 대입
        return AccountBalanceResponseDto.from(100000000L, account);
    }

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 삭제합니다.
     *
     * <p>해당하는 계좌가 존재하지 않는 경우 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @param accountId 삭제할 계좌 ID
     * @return 삭제된 계좌의 기본 정보를 담은 {@link AccountResponseDto}
     * @throws AccountException 계좌를 찾을 수 없는 경우 발생
     */
    @Override
    public AccountResponseDto deleteLinkedAccount(Long userId, Long accountId) {
        LinkedAccount account = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[DELETE] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        this.accountRepository.delete(account);

        return AccountResponseDto.from(account);
    }


}
