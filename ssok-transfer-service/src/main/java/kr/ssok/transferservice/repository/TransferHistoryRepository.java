package kr.ssok.transferservice.repository;

import kr.ssok.transferservice.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {

    /**
     * 주어진 계좌 ID와 기준일자 이후 송금 이력을 조회합니다.
     * 생성일 기준 최신순으로 정렬됩니다.
     *
     * @param accountId 계좌 ID
     * @param createdAt 기준일자 (3개월 전)
     * @return 송금 이력 리스트
     */
    List<TransferHistory> findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(Long accountId, LocalDateTime createdAt);
}
