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
import kr.ssok.accountservice.util.AccountIdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 사용자의 연동 계좌를 생성합니다.
     *
     * <p>계좌 유효성은 Redis에 캐싱된 값과 비교하여 확인되며,
     * 동일한 계좌가 이미 존재하는 경우 삭제 여부(isDeleted)에 따라 처리 방식이 달라집니다.</p>
     *
     * <ul>
     *     <li>Redis에 해당 계좌 정보가 존재하지 않으면 {@link AccountException}이 발생합니다.</li>
     *     <li>이미 존재하는 계좌가 있고 isDeleted가 false인 경우 {@link AccountException}이 발생합니다.</li>
     *     <li>이미 존재하지만 isDeleted가 true인 경우 해당 계좌는 복구되어 재사용됩니다.</li>
     *     <li>존재하지 않는 은행 코드나 계좌 타입 코드 요청 시 각각 {@link AccountException}이 발생합니다.</li>
     * </ul>
     *
     * @param userId 사용자 ID
     * @param createAccountRequestDto 생성할 계좌 정보
     * @return 생성된 또는 복구된 계좌 정보를 담은 {@link AccountResponseDto}
     * @throws AccountException 계좌 유효성 검증 실패, 중복 계좌 존재, 잘못된 코드 요청 등의 경우 발생
     */
    @Override
    @Transactional
    public AccountResponseDto createLinkedAccount(Long userId, CreateAccountRequestDto createAccountRequestDto) {
        // 계좌 유효성 검사를 위한 로직 - 캐싱된 redis와의 계좌 정보 비교
        Boolean isValidAccount = validateAccountOwnership(userId, createAccountRequestDto);

        // 계좌 정보가 존재하지 않을 시, 예외발생
        if (Boolean.FALSE.equals(isValidAccount) || isValidAccount == null) {
            throw new AccountException(AccountResponseStatus.ACCOUNT_CREATION_FORBIDDEN);
        }
        // 기존 계좌가 존재하는 지 확인
        AccountResponseDto existingAccount = this.accountRepository.findByAccountNumber(createAccountRequestDto.getAccountNumber())
                .map(existing -> {
                    // 기존 계좌 존재, isDeleted == false 일 때, 예외처리
                    if (!existing.getIsDeleted()) {
                        log.warn("[POST] Account already exists: {}", existing.getAccountNumber());
                        throw new AccountException(AccountResponseStatus.ACCOUNT_ALREADY_EXISTS);
                    }

                    // 기존 계좌 존재, isDeleted == false 일 때, isDeleted = true로 변경 (계좌 복구)
                    existing.markAsActive();

                    // LinkedAccount 객체를 AccountResponseDto로 매핑해서 리턴
                    return AccountResponseDto.from(existing);
                })
                .orElse(null);

        // isDeleted = true로 변경된 계좌 데이터가 있으면, 그대로 리턴
        if (existingAccount != null) { return existingAccount; }

        // 없을 시, 아래 로직 실행
        // 존재하지 않는 은행 코드 요청 시, 예외발생
        BankCode bankCode;
        try {
            bankCode = BankCode.fromIdx(createAccountRequestDto.getBankCode());
        } catch (IllegalArgumentException e) {
            log.warn("[POST] Invalid bank code: {}", createAccountRequestDto.getBankCode());
            throw new AccountException(AccountResponseStatus.INVALID_BANK_CODE);
        }

        // 존재하지 않는 계좌 타입 코드 요청 시, 예외발생
        AccountTypeCode accountTypeCode;
        try {
            accountTypeCode = AccountTypeCode.fromIdx(createAccountRequestDto.getAccountTypeCode());
        } catch (IllegalArgumentException e) {
            log.warn("[POST] Invalid account type code: {}", createAccountRequestDto.getAccountTypeCode());
            throw new AccountException(AccountResponseStatus.INVALID_ACCOUNT_TYPE);
        }

        LinkedAccount linkedAccount = LinkedAccount.builder()
                .accountNumber(createAccountRequestDto.getAccountNumber())
                .bankCode(bankCode)
                .userId(userId)
                .accountTypeCode(accountTypeCode)
                .build();

        this.accountRepository.save(linkedAccount);

        return AccountResponseDto.from(linkedAccount);
    }

    /**
     * 사용자 ID에 해당하는 모든 연동 계좌 목록을 비동기로 조회합니다.
     *
     * <p>오픈뱅킹 서버를 통해 각 계좌의 잔액을 병렬로 조회하며,
     * 잔액 조회 실패 시 해당 계좌의 잔액은 -1로 처리됩니다.</p>
     *
     * @param userId 사용자 ID
     * @return 연동 계좌 정보를 담은 {@link List}<{@link AccountBalanceResponseDto}>
     * @throws AccountException 사용자의 연동 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<AccountBalanceResponseDto>> findAllAccounts(Long userId) {
        List<LinkedAccount> linkedAccounts = this.accountRepository.findByUserIdAndIsDeletedFalse(userId);

        if (CollectionUtils.isEmpty(linkedAccounts)) {
            log.warn("[GET] Account not found: userId={}", userId);
            return CompletableFuture.failedFuture(new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND));
        }

        List<CompletableFuture<AccountBalanceResponseDto>> futures = linkedAccounts.stream()
                .map(account -> {
                    OpenBankingAccountBalanceRequestDto requestDto = OpenBankingAccountBalanceRequestDto.from(account);

                    return accountOpenBankingService.fetchAccountBalanceFromOpenBanking(requestDto)
                            .thenApply(balanceDto -> AccountBalanceResponseDto.from(
                                    balanceDto != null && balanceDto.getBalance() != null ? balanceDto.getBalance() : -1L,
                                    account))
                            .exceptionally(e -> {
                                log.error("[OPENBANKING][비동기] 잔액 조회 에러: {}", account.getAccountNumber(), e);
                                return AccountBalanceResponseDto.from(-1L, account);
                            });
                })
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 비동기로 상세 조회합니다.
     *
     * <p>오픈뱅킹 서버를 통해 해당 계좌의 잔액을 조회하며,
     * 잔액 조회 실패 시 잔액은 -1로 설정됩니다.</p>
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 연동 계좌 정보를 담은 {@link AccountBalanceResponseDto}
     * @throws AccountException 해당하는 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<AccountBalanceResponseDto> findAccountById(Long userId, Long accountId) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        OpenBankingAccountBalanceRequestDto requestDto = OpenBankingAccountBalanceRequestDto.from(linkedAccount);

        // WebClient 비동기 방식으로 잔액 조회
        return accountOpenBankingService.fetchAccountBalanceFromOpenBanking(requestDto)
                .thenApply(balanceDto -> AccountBalanceResponseDto.from(
                        balanceDto != null && balanceDto.getBalance() != null ? balanceDto.getBalance() : -1L,
                        linkedAccount))
                .exceptionally(e -> {
                    log.error("[OPENBANKING][비동기] 잔액 조회 에러: accountId={}, userId={}", accountId, userId, e);
                    return AccountBalanceResponseDto.from(-1L, linkedAccount);
                });
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
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[DELETE] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        if (Boolean.TRUE.equals(linkedAccount.getIsPrimaryAccount())) {
            log.warn("[DELETE] Attempted to delete primary account: accountId={}, userId={}", accountId, userId);
            throw new AccountException(AccountResponseStatus.ACCOUNT_PRIMARY_CANNOT_DELETE);    }

        linkedAccount.markAsDeleted();

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
    public AccountResponseDto updateLinkedAccountAlias(Long userId, Long accountId, UpdateAliasRequestDto updateAliasRequestDto) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[PATCH] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        String newAlias = updateAliasRequestDto.getAccountAlias();
        // 형식 제한 가능 (ex. newAlias.length() > 20))
        if (!StringUtils.hasText(newAlias)) {
            log.warn("[PATCH] Invalid account alias: {}", newAlias);
            throw new AccountException(AccountResponseStatus.INVALID_ACCOUNT_ALIAS);
        }

        linkedAccount.updateAlias(newAlias.trim());

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
    public AccountResponseDto updatePrimaryLinkedAccount(Long userId, Long accountId) {
        Optional<LinkedAccount> existingPrimaryOpt = this.accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId);

        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)
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
        existingPrimaryOpt.ifPresent(existing -> existing.updatePrimaryAccount(false));

        // 요청 계좌를 주계좌로 설정
        linkedAccount.updatePrimaryAccount(true);

        return AccountResponseDto.from(linkedAccount);
    }

    private Boolean validateAccountOwnership(Long userId, CreateAccountRequestDto dto) {
        String redisKey = AccountIdentifierUtil.buildLookupKey(userId);
        String lookupKey = AccountIdentifierUtil.buildLookupValue(
                dto.getBankCode(),
                dto.getAccountNumber(),
                dto.getAccountTypeCode()
        );

        // 해당 계좌의 존재 여부를 boolean으로 리턴
        return redisTemplate.opsForSet().isMember(redisKey, lookupKey);
    }
}
