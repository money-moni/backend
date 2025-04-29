package kr.ssok.transferservice.service;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.OpenBankingClient;
import kr.ssok.transferservice.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void 송금_요청이_성공하면_출금과_입금_내역을_모두_저장한다() {
        // Given
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When
        TransferResponseDto responseDto = this.transferService.transfer(userId, requestDto);

        // Then
        assertThat(responseDto.getSendAccountId()).isEqualTo(5L);
        assertThat(responseDto.getRecvAccountNumber()).isEqualTo("1111-111-1112");
        assertThat(responseDto.getAmount()).isEqualTo(15000L);

        // 출금 + 입금 내역이 모두 저장되었는지 검증 (2번 호출)
        verify(transferHistoryRepository, times(2)).save(any(TransferHistory.class));
    }

    @Test
    void 상대방_계좌ID가_없으면_출금내역만_저장한다() {
        // Given
        fakeAccountServiceClient.failRecvAccountId = true; // 상대방 계좌 ID 없는 경우
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When
        TransferResponseDto responseDto = this.transferService.transfer(userId, requestDto);

        // Then
        assertThat(responseDto.getSendAccountId()).isEqualTo(5L);
        assertThat(responseDto.getRecvAccountNumber()).isEqualTo("1111-111-1112");
        assertThat(responseDto.getAmount()).isEqualTo(15000L);

        // 출금 내역만 저장되었는지 검증 (1번 호출)
        verify(transferHistoryRepository, times(1)).save(any(TransferHistory.class));
    }

    @Test
    void 계좌_조회에_실패하면_ACCOUNT_LOOKUP_FAILED_예외를_던진다() {
        // Given
        fakeAccountServiceClient.failRecvAccountInfo = true;
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
        fakeOpenBankingClient.failTransfer = true;
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
        private boolean failRecvAccountInfo = false;
        private boolean failRecvAccountId = false;

        @Override
        public BaseResponse<AccountResponse.Result> getAccountInfo(Long accountId, Long userId) {
            if (failRecvAccountInfo) {
                return new BaseResponse<>(false, 4001, "계좌 조회 실패", null);
            }
            return new BaseResponse<>(true, 2000, "계좌 조회 성공",
                    new AccountResponse.Result("1111-111-1111"));
        }

        @Override
        public BaseResponse<AccountIdResponse.Result> getAccountId(String accountNumber) {
            if (failRecvAccountId) {
                return new BaseResponse<>(true, 2001, "계좌 ID 없음", null); // accountId 없으면 code=2001
            }
            return new BaseResponse<>(true, 2000, "계좌 ID 조회 성공",
                    new AccountIdResponse.Result(10L));
        }

        @Override
        public BaseResponse<AccountIdsResponse.Result> getAccountIdsByUserId(Long userId) {
            return null;
        }
    }

    /**
     * Fake: 오픈뱅킹 송금 요청을 흉내내는 객체
     */
    private static class FakeOpenBankingClient implements OpenBankingClient {
        private boolean failTransfer = false;

        @Override
        public BaseResponse<Object> sendTransferRequest(OpenBankingTransferRequestDto requestBody) {
            if (failTransfer) {
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
                .sendName("테스트송신자")
                .recvAccountNumber("1111-111-1112")
                .recvBankCode(1)
                .amount(15000L)
                .recvName("테스트수신자")
                .build();
    }
}