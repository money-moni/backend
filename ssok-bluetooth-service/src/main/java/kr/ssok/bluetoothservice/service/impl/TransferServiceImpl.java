package kr.ssok.bluetoothservice.service.impl;

import kr.ssok.bluetoothservice.client.TransferServiceClient;
import kr.ssok.bluetoothservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.bluetoothservice.dto.request.TransferRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.TransferService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Bluetooth 송금 서비스 구현체
 * UUID로 수신자 조회 후, OpenFeign을 통해 TransferService에 송금 요청을 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TransferServiceClient transferServiceClient;

    /**
     * 블루투스 송금 처리
     *
     * @param senderUserId 요청자(userId)
     * @param requestDto   송금 요청 정보(UUID, 출금 계좌 등)
     * @return 송금 응답 결과 (수신자 이름, 금액 등)
     * @throws BluetoothException UUID 없음, 매핑 실패, 송금 실패, Redis 오류 등 발생 가능
     */
    @Override
    public BluetoothTransferResponseDto transfer(Long senderUserId, BluetoothTransferRequestDto requestDto) {
        log.info("[BLUETOOTH-TRANSFER] 송금 요청 시작: senderUserId={}, recvUuid={}, amount={}",
                senderUserId, requestDto.getRecvUuid(), requestDto.getAmount());

        // UUID 유효성 검증
        if (requestDto.getRecvUuid() == null || requestDto.getRecvUuid().isBlank()) {
            log.warn("[BLUETOOTH-TRANSFER] UUID 누락");
            throw new BluetoothException(BluetoothResponseStatus.UUID_REQUIRED);
        }

        try {
            long startTime = System.currentTimeMillis();

            // Redis에서 UUID → userId 조회
            String recvUserIdStr = redisTemplate.opsForValue().get("uuid:" + requestDto.getRecvUuid());
            if (recvUserIdStr == null) {
                log.warn("[BLUETOOTH-TRANSFER] 일치하는 UUID 없음: {}", requestDto.getRecvUuid());
                throw new BluetoothException(BluetoothResponseStatus.NO_MATCH_FOUND);
            }

            Long recvUserId = Long.parseLong(recvUserIdStr);

            // Feign 전송용 DTO 구성
            TransferRequestDto dto = TransferRequestDto.builder()
                    .sendAccountId(requestDto.getSendAccountId())
                    .sendBankCode(requestDto.getSendBankCode())
                    .sendName(requestDto.getSendName())
                    .recvUserId(recvUserId)
                    .amount(requestDto.getAmount())
                    .build();

            log.info("[BLUETOOTH-TRANSFER] 송금 요청 전송");

            // Feign Client 호출
            BaseResponse<BluetoothTransferResponseDto> response = transferServiceClient.bluetoothTransfer(senderUserId, dto);

            long endTime = System.currentTimeMillis();
            log.info("[BLUETOOTH-TRANSFER] 송금 요청 완료 (소요 시간: {}ms)", (endTime - startTime));

            if (!response.getIsSuccess()) {
                log.error("[BLUETOOTH-TRANSFER] 송금 실패: {}", response.getMessage());
                throw new BluetoothException(BluetoothResponseStatus.TRANSFER_FAILED);
            }

            log.info("[BLUETOOTH-TRANSFER] 송금 성공: accountId={}, amount={}",
                    response.getResult().getSendAccountId(), response.getResult().getAmount());

            return response.getResult();

        } catch (DataAccessException e) {
            log.error("[BLUETOOTH-TRANSFER] Redis 접근 실패", e);
            throw new BluetoothException(BluetoothResponseStatus.REDIS_ACCESS_FAILED);
        }
    }
}
