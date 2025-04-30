package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.request.UpdateAliasRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import kr.ssok.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    private final AccountOpenBankingService accountOpenBankingService;

    /**
     * 사용자의 연동 계좌를 생성합니다.
     *
     * <p>이미 동일한 계좌 번호가 존재하는 경우 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @param createAccountRequestDto 계좌 생성 요청 DTO
     * @return 생성된 계좌 정보를 담은 응답 DTO
     * @throws AccountException 이미 동일한 계좌가 존재하는 경우 발생
     */
    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<AccountBalanceResponseDto> findAllAccounts(Long userId) {
        List<LinkedAccount> linkedAccounts = this.accountRepository.findByUserId(userId);

        if (linkedAccounts.isEmpty()) {
            log.warn("[GET] Account not found: userId={}", userId);
            throw new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
        }

        // 병렬 처리
        return linkedAccounts.parallelStream()
                .map(account -> {
                    try {
                        OpenBankingAccountBalanceRequestDto requestDto =
                                OpenBankingAccountBalanceRequestDto.from(account);

                        Long balance = accountOpenBankingService
                                .fetchAccountBalanceFromOpenBanking(requestDto)
                                .getBalance();
                        return AccountBalanceResponseDto.from(balance, account);
                    } catch (Exception e) {
                        log.error("[OPENBANKING][병렬] 잔액 조회 실패: {}", account.getAccountNumber());
                        return AccountBalanceResponseDto.from(-1L, account);
                    }
                })
                .toList();
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
    @Transactional(readOnly = true)
    public AccountBalanceResponseDto findAccountById(Long userId, Long accountId) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        OpenBankingAccountBalanceRequestDto requestDto =
                OpenBankingAccountBalanceRequestDto.from(linkedAccount);

        Long balance;
        try {
            balance = this.accountOpenBankingService
                    .fetchAccountBalanceFromOpenBanking(requestDto)
                    .getBalance();
        } catch (Exception e) {
            log.error("[OPENBANKING] 잔액 조회 실패: accountId={}, userId={}", accountId, userId, e);
            balance = -1L;
        }
        return AccountBalanceResponseDto.from(balance, linkedAccount);
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
    @Transactional
    public AccountResponseDto deleteLinkedAccount(Long userId, Long accountId) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[DELETE] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        this.accountRepository.delete(linkedAccount);

        return AccountResponseDto.from(linkedAccount);
    }

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌의 별칭(alias)을 수정합니다.
     *
     * <p>요청한 사용자 ID와 계좌 ID가 일치하는 계좌가 존재하지 않으면 {@link AccountException}을 발생시킵니다.</p>
     *
     * @param userId 사용자 ID
     * @param accountId 별명을 수정할 계좌 ID
     * @param updateAliasRequestDto 새 별칭 정보를 담고 있는 요청 DTO
     * @return 별칭이 수정된 계좌 정보를 담은 {@link AccountResponseDto}
     * @throws AccountException 계좌를 찾을 수 없는 경우 발생
     */
    @Override
    @Transactional
    public AccountResponseDto updateAccountAlias(Long userId, Long accountId, UpdateAliasRequestDto updateAliasRequestDto) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[PATCH] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });
        linkedAccount.updateAlias(updateAliasRequestDto.getAccountAlias());

        return AccountResponseDto.from(linkedAccount);
    }

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 주계좌(primary account)로 설정합니다.
     *
     * <p>기존 주계좌가 있으면 isPrimaryAccount 를 false로 바꾸고,
     * 해당 계좌의 isPrimaryAccount를 true로 설정합니다.
     * 계좌가 존재하지 않거나 유효하지 않은 요청일 경우 {@link AccountException}이 발생합니다.</p>
     *
     * @param userId 사용자 ID
     * @param accountId 주계좌로 설정할 계좌 ID
     * @return 주계좌로 변경된 계좌 정보를 담은 {@link AccountResponseDto}
     * @throws AccountException 계좌를 찾을 수 없거나 기존 주 계좌에 주 계좌 설정을 요청할 경우 발생
     */
    @Override
    @Transactional
    public AccountResponseDto updatePrimaryAccount(Long userId, Long accountId) {
        Optional<LinkedAccount> existingPrimaryOpt = this.accountRepository.findByUserIdAndIsPrimaryAccountTrue(userId);

        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[PATCH] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        // 동일 계좌일 경우 예외 처리
        existingPrimaryOpt
                .filter(existing -> existing.getAccountId().equals(accountId))
                .ifPresent(existing -> {
                    log.warn("[PATCH] Account {} is already the primary account", accountId);
                    throw new AccountException(AccountResponseStatus.ACCOUNT_ALREADY_PRIMARY);
                });

        // 기존 주계좌가 존재하면 해제
        existingPrimaryOpt.ifPresent(existing -> {
            existing.updatePrimaryAccount(false);
        });

        // 요청 계좌를 주계좌로 설정
        linkedAccount.updatePrimaryAccount(true);

        return AccountResponseDto.from(linkedAccount);
    }


}
