package kr.ssok.transferservice.service;

import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;

import java.util.List;

/**
 * 송금 이력 조회 서비스 인터페이스
 */
public interface TransferHistoryService {

    /**
     * 주어진 계좌 ID로 3개월 이내 송금 이력을 조회
     *
     * @param accountId 계좌 ID
     * @return 송금 이력 리스트
     */
    List<TransferHistoryResponseDto> getTransferHistories(Long accountId);
}