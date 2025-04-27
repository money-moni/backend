package kr.ssok.transferservice.service;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.OpenBankingClient;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TransferService의 단위 테스트
 * - AccountServiceClient, OpenBankingClient는 Fake 사용
 * - TransferHistoryRepository는 Mock 사용
 */
public class TransferServiceTest {

    private TransferServiceImpl transferService;
    private TransferHistoryRepository transferHistoryRepository;
    private FakeAccountServiceClient fakeAccountServiceClient;
    private FakeOpenBankingClient fakeOpenBankingClient;

    @BeforeEach
    void setUp() {
        // Given: 모든 테스트 시작 전에 깨끗한 Fake 및 Mock 객체를 준비
        this.fakeAccountServiceClient = new FakeAccountServiceClient();
        this.fakeOpenBankingClient = new FakeOpenBankingClient();
        this.transferHistoryRepository = mock(TransferHistoryRepository.class);

        this.transferService = new TransferServiceImpl(
                fakeAccountServiceClient,
                fakeOpenBankingClient,
                transferHistoryRepository
        );
    }

    @Test
    void 송금_요청이_성공하면_송금내역을_저장하고_정상_응답을_반환한다() {
        // Given: 송금 요청 정보 준비
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When: 송금 서비스 호출
        TransferResponseDto responseDto = this.transferService.transfer(userId, requestDto);

        // Then: 응답 값 검증
        assertThat(responseDto.getSendAccountId()).isEqualTo(5L);
        assertThat(responseDto.getRecvAccountNumber()).isEqualTo("1111-111-1112");
        assertThat(responseDto.getAmount()).isEqualTo(15000L);

        // Then: 송금 이력이 저장되었는지 검증
        verify(transferHistoryRepository, times(1)).save(any(TransferHistory.class));
    }

    @Test
    void 계좌_조회에_실패하면_ACCOUNT_LOOKUP_FAILED_예외를_던진다() {
        // Given
        fakeAccountServiceClient.failNext = true;
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED.getMessage());
                });
    }


    @Test
    void 오픈뱅킹_송금에_실패하면_REMITTANCE_FAILED_예외를_던진다() {
        // Given
        fakeOpenBankingClient.failNext = true;
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.REMITTANCE_FAILED.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.REMITTANCE_FAILED.getMessage());
                });
    }

    @Test
    void 송금금액이_0원이면_INVALID_TRANSFER_AMOUNT_예외를_던진다() {
        // Given
        TransferRequestDto requestDto = TransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .recvAccountNumber("1111-111-1112")
                .recvBankCode(1)
                .amount(0L)
                .recvName("테스트수신자")
                .build();
        Long userId = 2L;

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.INVALID_TRANSFER_AMOUNT.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.INVALID_TRANSFER_AMOUNT.getMessage());
                });
    }

    // ----------------------------------------------------------
    // 테스트용 Fake 클래스들
    // ----------------------------------------------------------

    /**
     * Fake: 계좌 서비스 응답을 흉내내는 객체
     */
    private static class FakeAccountServiceClient implements AccountServiceClient {
        private boolean failNext = false;

        @Override
        public BaseResponse<AccountResponse.Result> getAccountInfo(Long accountId, Long userId) {
            if (failNext) {
                return new BaseResponse<>(false, 400, "계좌 조회 실패", null);
            }
            return new BaseResponse<>(true, 200, "계좌 조회 성공",
                    new AccountResponse.Result("1111-111-1111"));
        }
    }

    /**
     * Fake: 오픈뱅킹 송금 요청을 흉내내는 객체
     */
    private static class FakeOpenBankingClient implements OpenBankingClient {
        private boolean failNext = false;

        @Override
        public BaseResponse<Object> sendTransferRequest(Map<String, Object> requestBody) {
            if (failNext) {
                return new BaseResponse<>(false, 400, "송금 실패", null);
            }
            return new BaseResponse<>(true, 200, "송금 성공", null);
        }
    }

    // ----------------------------------------------------------
    // 헬퍼 메서드
    // ----------------------------------------------------------

    private TransferRequestDto 기본_송금요청() {
        return TransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .recvAccountNumber("1111-111-1112")
                .recvBankCode(1)
                .amount(15000L)
                .recvName("테스트수신자")
                .build();
    }
}