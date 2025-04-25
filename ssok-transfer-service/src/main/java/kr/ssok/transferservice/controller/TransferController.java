package kr.ssok.transferservice.controller;

import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 송금 요청을 처리하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/openbank/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * 송금 API 요청 처리
     *
     * @param requestDto 클라이언트에서 받은 송금 요청 DTO
     * @param userId     Gateway에서 전달한 사용자 ID (헤더)
     * @return 송금 결과를 담은 BaseResponse 객체
     */
    @PostMapping
    public ResponseEntity<BaseResponse<TransferResponseDto>> transfer(
            @RequestBody TransferRequestDto requestDto,
            @RequestHeader("X-user-Id") Long userId
    ) {
        TransferResponseDto result = this.transferService.transfer(userId, requestDto);
        return ResponseEntity.ok(new BaseResponse<>(true, 200, "송금에 성공했습니다.", result));
    }
}
