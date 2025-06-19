package kr.ssok.transferservice.service.impl;

import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;
import kr.ssok.transferservice.dto.response.TransferRecentHistoryResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.grpc.client.AccountServiceClient;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.TransferHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 송금 이력 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferHistoryServiceImpl implements TransferHistoryService {

    private final AccountServiceClient accountServiceClient;
    private final TransferHistoryRepository transferHistoryRepository;

    /**
     * 계좌 ID로 3개월 이내 송금 이력을 조회
     *
     * @param accountId 계좌 ID
     * @return 송금 이력 응답 DTO 리스트
     */
    @Override
    public List<TransferHistoryResponseDto> getTransferHistories(Long accountId) {
        // 0. 계좌 ID 유효성 검증
        if (accountId == null) {
            throw new TransferException(TransferResponseStatus.INVALID_ACCOUNT_ID);
        }

        // 1. 3개월 전 시간 계산
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        // 2. 3개월 송금 이력 조회
        long start = System.currentTimeMillis();
        List<TransferHistory> histories = transferHistoryRepository
                .findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(accountId, threeMonthsAgo);
        long end = System.currentTimeMillis();
        log.info("[SSOK-TRANSFER-HISTORY] 3개월 송금 이력 조회 시간: {}ms", end - start);

        return histories.stream()
                .map(history -> TransferHistoryResponseDto.builder()
                        .transferId(history.getId())
                        .transferType(history.getTransferType())
                        .transferMoney(history.getTransferMoney())
                        .currencyCode(history.getCurrencyCode())
                        .transferMethod(history.getTransferMethod())
                        .counterpartAccount(history.getCounterpartAccount())
                        .counterpartName(history.getCounterpartName())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID로 최근 송금한 상대 목록 조회
     *
     * @param userId 사용자 ID
     * @return 송금 상대 목록
     */
    @Override
    public List<TransferCounterpartResponseDto> getRecentCounterparts(Long userId) {
        if (userId == null) {
            throw new TransferException(TransferResponseStatus.INVALID_USER_ID);
        }

        List<Long> accountIds = getAccountIdsByUserId(userId);
        if (accountIds.isEmpty()) {
            log.warn("[SSOK-TRANSFER] 사용자 ID {}에 해당하는 계좌가 없습니다.", userId);
            return List.of();
        }

        long start = System.currentTimeMillis();
        List<TransferCounterpartResponseDto> result = this.transferHistoryRepository.findRecentCounterparts(accountIds);
        long end = System.currentTimeMillis();
        log.info("[SSOK-TRANSFER-HISTORY] 최근 송금 상대 조회 시간: {}ms", end - start);
        log.info("[SSOK-TRANSFER-HISTORY] 사용자 ID {}의 계좌들로부터 최근 송금 상대 {}건 조회", userId, result.size());

        return result;
    }

    @Override
    public List<TransferRecentHistoryResponseDto> getRecentHistories(Long userId) {
        if (userId == null) {
            throw new TransferException(TransferResponseStatus.INVALID_USER_ID);
        }

        List<Long> accountIds = getAccountIdsByUserId(userId);
        if (accountIds.isEmpty()) {
            log.warn("[SSOK-TRANSFER-HISTORY] 사용자 ID {}의 계좌가 존재하지 않아 최근 송금 이력을 조회할 수 없습니다.", userId);
            return List.of();
        }

        long start = System.currentTimeMillis();
        // 3. 최근 송금 이력 3건 조회
        List<TransferHistory> histories = transferHistoryRepository
                .findTop3ByAccountIdInOrderByCreatedAtDesc(accountIds);
        long end = System.currentTimeMillis();
        log.info("[SSOK-TRANSFER-HISTORY] 최근 송금 이력 3건 조회 시간: {}ms", end - start);

        return histories.stream()
                .map(history -> TransferRecentHistoryResponseDto.builder()
                        .transferId(history.getId())
                        .transferType(history.getTransferType())
                        .counterpartName(history.getCounterpartName())
                        .transferMoney(history.getTransferMoney())
                        .currencyCode(history.getCurrencyCode())
                        .transferMethod(history.getTransferMethod())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID로 계좌 ID 목록 조회 (공통 로직 분리)
     *
     * @param userId 사용자 ID
     * @return 계좌 ID 리스트 (없으면 빈 리스트 반환)
     */
    private List<Long> getAccountIdsByUserId(Long userId) {
        // 1. 계좌 서비스에서 사용자 ID로 모든 계좌 ID 조회
        long start = System.currentTimeMillis();
//        BaseResponse<List<AccountIdResponseDto>> accountListResponse =
//                this.accountServiceClient.getAccountIdsByUserId(userId.toString());
        List<AccountIdResponseDto> accountListResponse =
                this.accountServiceClient.getAccountIdsByUserId(userId.toString());
        long end = System.currentTimeMillis();
        log.info("[SSOK-ACCOUNT] 사용자 계좌 ID 조회 시간: {}ms", end - start);

        // NPE 방지
        if (accountListResponse == null || accountListResponse.isEmpty()) {
            log.warn("[SSOK-ACCOUNT] 사용자 ID {}에 해당하는 계좌가 없거나 응답이 비어있습니다.", userId);
            return List.of();
        }

        // 2. 계좌 ID 리스트 추출
        return accountListResponse.stream()
                .map(AccountIdResponseDto::getAccountId)
                .collect(Collectors.toList());
    }
}