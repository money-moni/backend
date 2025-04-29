package kr.ssok.transferservice.repository.custom.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.entity.QTransferHistory;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.repository.custom.TransferHistoryRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 송금 이력 QueryDSL 쿼리 구현체
 */
@Repository
@RequiredArgsConstructor
public class TransferHistoryRepositoryImpl implements TransferHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 주어진 계좌 ID 리스트를 기반으로 최근 송금(출금)한 상대방 목록을 조회
     * - 조회 기준: 일반(GENERAL) 송금이면서, 출금(WITHDRAWAL) 타입인 건만.
     * - 중복 상대방 계좌는 제거하고, 가장 최근 송금 시점을 기준으로 정렬
     *
     * @param accountIds 조회 대상 계좌 ID 리스트
     * @return 중복 제거된 송금 상대방 목록 (TransferCounterpartResponseDto 리스트)
     */
    @Override
    public List<TransferCounterpartResponseDto> findRecentCounterparts(List<Long> accountIds) {
        QTransferHistory history = QTransferHistory.transferHistory;

        if (accountIds == null || accountIds.isEmpty()) {
            // 빈 결과 리턴
            return List.of();
        }

        // 1. DB에서 조건에 맞는 송금 이력만 조회하고 바로 DTO로 매핑
        List<TransferCounterpartResponseDto> fetchedResults = queryFactory
                .select(
                        // Projections.constructor을 통해 SELECT 결과를 바로 DTO 생성자로 매핑
                        Projections.constructor(
                                TransferCounterpartResponseDto.class,
                                history.counterpartName,
                                history.counterpartAccount,
                                history.createdAt.max() // max(createdAt)로 최신 송금 시간
                        )
                )
                .from(history)
                .where(
                        history.accountId.in(accountIds),                   // 조회할 계좌 ID 중 하나
                        history.transferType.eq(TransferType.WITHDRAWAL),   // 출금 건만
                        history.transferMethod.eq(TransferMethod.GENERAL)   // 일반 송금만
                )
                .groupBy(history.counterpartAccount, history.counterpartName) // 상대방 계좌번호 + 이름 기준 그룹핑
                .orderBy(history.createdAt.max().desc()) // 최근 송금 시점 기준 정렬
                .fetch(); // SQL 실행 및 결과 fetch

        return fetchedResults;
    }
}
