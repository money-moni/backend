package kr.ssok.transferservice.repository.custom;

import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;

import java.util.List;

/**
 * 커스텀 송금 이력 조회 Repository 인터페이스
 */
public interface TransferHistoryRepositoryCustom {
    /**
     * 계좌 ID 리스트로 최근 송금한 상대 계좌 목록 조회
     *
     * @param accountIds 조회할 계좌 ID 목록
     * @return 송금 상대 목록
     */
    List<TransferCounterpartResponseDto> findRecentCounterparts(List<Long> accountIds);
}
