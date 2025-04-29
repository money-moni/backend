package kr.ssok.transferservice.service.impl;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
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
        List<TransferHistory> histories = transferHistoryRepository
                .findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(accountId, threeMonthsAgo);

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

        // 1. 계좌 서비스에서 사용자 ID로 모든 계좌 ID 조회
        BaseResponse<AccountServiceClient.AccountIdsResponse.Result> accountListResponse =
                this.accountServiceClient.getAccountIdsByUserId(userId);

        // NPE 방지
        if (!accountListResponse.getIsSuccess() ||
                accountListResponse.getResult() == null ||
                accountListResponse.getResult().getAccountIds() == null ||
                accountListResponse.getResult().getAccountIds().isEmpty()) {
            return List.of(); // 비어 있는 리스트 반환
        }

        // 2. 계좌 ID 목록으로 송금 상대 조회 (QueryDSL)
        List<Long> accountIds = accountListResponse.getResult().getAccountIds();
        return this.transferHistoryRepository.findRecentCounterparts(accountIds);
    }
}