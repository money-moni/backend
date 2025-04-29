package kr.ssok.transferservice.controller;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.service.TransferHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 송금 이력 조회를 담당하는 REST 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
public class TransferHistoryController {

    private final TransferHistoryService transferHistoryService;

    /**
     * 계좌 ID를 기준으로 3개월 이내 송금 거래 내역 조회 API
     *
     * @param accountId 조회할 계좌 ID
     * @return 송금 이력 리스트를 포함한 응답
     */
    @GetMapping("/histories")
    public ResponseEntity<BaseResponse<List<TransferHistoryResponseDto>>> getTransferHistories(
            @RequestParam Long accountId) {
        List<TransferHistoryResponseDto> result = transferHistoryService.getTransferHistories(accountId);
        return ResponseEntity.ok(new BaseResponse<>(TransferResponseStatus.TRANSFER_HISTORY_SUCCESS, result));
    }

    /**
     * 사용자 ID로 최근 송금한 상대 계좌 목록 조회
     *
     * @param userId Gateway에서 전달된 사용자 ID
     * @return BaseResponse로 감싼 송금 상대 목록
     */
    @GetMapping("/counterparts")
    public ResponseEntity<BaseResponse<List<TransferCounterpartResponseDto>>> getCounterparts(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<TransferCounterpartResponseDto> result = transferHistoryService.getRecentCounterparts(userId);
        return ResponseEntity.ok(new BaseResponse<>(TransferResponseStatus.TRANSFER_COUNTERPART_SUCCESS, result));
    }
}