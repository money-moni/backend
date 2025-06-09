package kr.ssok.bluetoothservice.service;

import kr.ssok.bluetoothservice.client.TransferServiceClient;
import kr.ssok.bluetoothservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.bluetoothservice.dto.request.TransferRequestDto;
import kr.ssok.bluetoothservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.bluetoothservice.exception.BluetoothException;
import kr.ssok.bluetoothservice.exception.BluetoothResponseStatus;
import kr.ssok.bluetoothservice.service.impl.TransferServiceImpl;
import kr.ssok.common.exception.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * <h3>TransferServiceImpl 단위 테스트</h3>
 * Redis UUID 매핑 및 Feign 송금 위임 기능 검증
 */
class TransferServiceImplTest {

    private RedisTemplate<String, String> redisTemplate;
    private TransferServiceClient transferServiceClient;
    private TransferServiceImpl transferService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        transferServiceClient = mock(TransferServiceClient.class);
        transferService = new TransferServiceImpl(redisTemplate, transferServiceClient);
    }

    /**
     * 성공 시: UUID 조회 성공 → 송금 요청 성공
     */
    @Test
    void UUID로_수신자_매핑_후_송금_요청에_성공한다() {
        // given
        Long senderUserId = 1L;
        String recvUuid = "abc-uuid";
        Long recvUserId = 10L;

        BluetoothTransferRequestDto request = BluetoothTransferRequestDto.builder()
                .recvUuid(recvUuid)
                .sendAccountId(123L)
                .sendBankCode(88)
                .sendName("홍길동")
                .amount(10000L)
                .build();

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("uuid:" + recvUuid)).willReturn(String.valueOf(recvUserId));

        BluetoothTransferResponseDto expectedResponse = BluetoothTransferResponseDto.builder()
                .sendAccountId(123L)
                .recvName("김*희")
                .amount(10000L)
                .build();

        BaseResponse<BluetoothTransferResponseDto> baseResponse = new BaseResponse<>(true, 2000, "성공", expectedResponse);
        given(transferServiceClient.bluetoothTransfer(eq(senderUserId), any(TransferRequestDto.class)))
                .willReturn(baseResponse);

        // when
        BluetoothTransferResponseDto result = transferService.transfer(senderUserId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRecvName()).isEqualTo("김*희");
        assertThat(result.getAmount()).isEqualTo(10000L);
    }

    /**
     * 실패: UUID가 비어 있는 경우 예외 발생
     */
    @Test
    void UUID가_비어_있으면_예외가_발생한다() {
        BluetoothTransferRequestDto request = BluetoothTransferRequestDto.builder()
                .recvUuid("")
                .build();

        assertThatThrownBy(() -> transferService.transfer(1L, request))
                .isInstanceOf(BluetoothException.class)
                .hasMessageContaining(BluetoothResponseStatus.UUID_REQUIRED.getMessage());
    }

    /**
     * 실패: UUID가 Redis에 존재하지 않음
     */
    @Test
    void Redis에_UUID가_없으면_예외가_발생한다() {
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("uuid:missing")).willReturn(null);

        BluetoothTransferRequestDto request = BluetoothTransferRequestDto.builder()
                .recvUuid("missing")
                .build();

        assertThatThrownBy(() -> transferService.transfer(1L, request))
                .isInstanceOf(BluetoothException.class)
                .hasMessageContaining(BluetoothResponseStatus.NO_MATCH_FOUND.getMessage());
    }
}
