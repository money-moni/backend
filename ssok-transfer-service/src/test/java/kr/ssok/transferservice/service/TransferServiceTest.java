package kr.ssok.transferservice.service;

import kr.ssok.transferservice.client.*;
import kr.ssok.transferservice.client.dto.response.*;
import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.webclient.OpenBankingApiClient;
import kr.ssok.transferservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.grpc.client.AccountService;
import kr.ssok.transferservice.kafka.producer.NotificationProducer;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.impl.TransferServiceImpl;
import kr.ssok.transferservice.service.impl.helper.AccountInfoResolver;
import kr.ssok.transferservice.service.impl.helper.TransferHistoryRecorder;
import kr.ssok.transferservice.service.impl.helper.TransferNotificationSender;
import kr.ssok.transferservice.service.impl.validator.TransferValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    private FakeOpenBankingWebClient fakeOpenBankingWebClient;
    private TransferNotificationSender notificationSender;
    private AccountInfoResolver accountInfoResolver;
    private TransferHistoryRecorder transferHistoryRecorder;
    private TransferValidator transferValidator;

    @BeforeEach
    void setUp() {
        this.fakeAccountServiceClient = new FakeAccountServiceClient();
        this.fakeOpenBankingWebClient = new FakeOpenBankingWebClient();
        this.transferHistoryRepository = mock(TransferHistoryRepository.class);

        this.notificationSender = new TransferNotificationSender(
                mock(NotificationProducer.class),
                mock(NotificationServiceClient.class));
        this.accountInfoResolver = new AccountInfoResolver(fakeAccountServiceClient);
        this.transferHistoryRecorder = new TransferHistoryRecorder(transferHistoryRepository);
        this.transferValidator = new TransferValidator();

        this.transferService = new TransferServiceImpl(
                transferValidator,
                accountInfoResolver,
                transferHistoryRecorder,
                notificationSender,
                fakeOpenBankingWebClient,
                fakeAccountServiceClient
        );
    }

    @Test
    void 송금_요청이_성공하면_출금과_입금_내역을_모두_저장한다() {
        // Given
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When
        TransferResponseDto responseDto = this.transferService.transfer(userId, requestDto, TransferMethod.GENERAL).join();

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
        TransferResponseDto responseDto = this.transferService.transfer(userId, requestDto, TransferMethod.GENERAL).join();

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
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto, TransferMethod.GENERAL))
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
        fakeOpenBankingWebClient.failTransfer = true;
        TransferRequestDto requestDto = 기본_송금요청();
        Long userId = 2L;

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto, TransferMethod.GENERAL).join())
                .hasCauseInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex.getCause();
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
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto, TransferMethod.GENERAL))
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
    private static class FakeAccountServiceClient implements AccountService {
        private boolean failRecvAccountInfo = false;
        private boolean failRecvAccountId = false;

        @Override
        public AccountResponseDto getAccountInfo(Long accountId, String userId) {
            if (failRecvAccountInfo) throw new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
            return new AccountResponseDto("1111-111-1111");
        }

        @Override
        public AccountIdResponseDto getAccountId(String accountNumber) {
            if (failRecvAccountId) return null;
            return new AccountIdResponseDto(10L, 1L);
        }

        @Override
        public List<AccountIdResponseDto> getAccountIdsByUserId(String userId) {
            // TransferServiceTest 에서 쓰이지 않으면 빈 구현
            return List.of();
        }

        @Override
        public PrimaryAccountResponseDto getPrimaryAccountInfo(String userId) {
            if (failRecvAccountInfo) throw new TransferException(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED);
            return PrimaryAccountResponseDto.builder()
                    .accountId(10L)
                    .accountNumber("1111-111-1112")
                    .bankCode(1)
                    .username("테스트수신자")
                    .build();
        }
    }

    private static class FakeOpenBankingWebClient implements OpenBankingApiClient {
        boolean failTransfer = false;

        @Override
        public Mono<OpenBankingResponse> sendTransferRequest(OpenBankingTransferRequestDto req) {
            // not used in this test
            return Mono.empty();
        }
        @Override
        public CompletableFuture<OpenBankingResponse> sendTransferRequestAsync(OpenBankingTransferRequestDto req) {
            if (failTransfer) {
                return CompletableFuture.completedFuture(
                        new OpenBankingResponse(false, "FAIL", "error", Map.of()));
            }
            return CompletableFuture.completedFuture(
                    new OpenBankingResponse(true, "2000", "ok", Map.of(
                            "transactionId","txid","status","COMPLETED","message","ok"))); }
    }

    /**
     * Fake: 오픈뱅킹 송금 요청을 흉내내는 객체
     */
    private static class FakeOpenBankingClient implements OpenBankingClient {
        private boolean failTransfer = false;
        private String errorCode = "TRANSFER002";  // 송금 에러 코드

        @Override
        public OpenBankingResponse sendTransferRequest(String apiKey, OpenBankingTransferRequestDto requestBody) {
            if (failTransfer) {
                Map<String, Object> result = Map.of(
                        "transactionId", "46f338bd-2090-4e15-9df3-2efb103f6f15",
                        "status", "FAILED",
                        "message", "송금 처리 중 오류가 발생했습니다."
                );
                return new OpenBankingResponse(false, errorCode, "송금 실패", result);
            }

            Map<String, Object> result = Map.of(
                    "transactionId", "abc123",
                    "status", "COMPLETED",
                    "message", "송금이 성공적으로 완료되었습니다."
            );
            return new OpenBankingResponse(true, "2000", "송금 성공", result);
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

    @Test
    void 블루투스_송금이_성공하면_출금과_입금_내역을_모두_저장한다() {
        // Given
        BluetoothTransferRequestDto requestDto = 기본_블루투스_송금요청();
        Long userId = 3L;

        // When
        BluetoothTransferResponseDto responseDto = this.transferService.bluetoothTransfer(userId, requestDto, TransferMethod.BLUETOOTH).join();

        // Then
        assertThat(responseDto.getSendAccountId()).isEqualTo(5L);
        assertThat(responseDto.getRecvName()).isEqualTo("테*트수신자");
        assertThat(responseDto.getAmount()).isEqualTo(15000L);

        // 출금 + 입금 내역이 모두 저장되었는지 검증 (2번 호출)
        verify(transferHistoryRepository, times(2)).save(any(TransferHistory.class));
    }

    @Test
    void 블루투스_계좌_조회에_실패하면_ACCOUNT_LOOKUP_FAILED_예외를_던진다() {
        // Given
        fakeAccountServiceClient.failRecvAccountInfo = true;
        BluetoothTransferRequestDto requestDto = 기본_블루투스_송금요청();
        Long userId = 3L;

        // When & Then
        assertThatThrownBy(() -> transferService.bluetoothTransfer(userId, requestDto, TransferMethod.BLUETOOTH))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED.getMessage());
                });
    }

    @Test
    void 블루투스_오픈뱅킹_송금에_실패하면_REMITTANCE_FAILED_예외를_던진다() {
        // Given
        fakeOpenBankingWebClient.failTransfer = true;
        BluetoothTransferRequestDto requestDto = 기본_블루투스_송금요청();
        Long userId = 3L;

        // When & Then
        assertThatThrownBy(() -> transferService.bluetoothTransfer(userId, requestDto, TransferMethod.BLUETOOTH).join())
                .hasCauseInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex.getCause();
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.REMITTANCE_FAILED.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.REMITTANCE_FAILED.getMessage());
                });
    }

    @Test
    void 블루투스_송금금액이_0원이면_INVALID_TRANSFER_AMOUNT_예외를_던진다() {
        // Given
        BluetoothTransferRequestDto requestDto = BluetoothTransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .sendName("테스트송신자")
                .recvUserId(10L)
                .amount(0L)
                .build();
        Long userId = 3L;

        // When & Then
        assertThatThrownBy(() -> transferService.bluetoothTransfer(userId, requestDto, TransferMethod.BLUETOOTH))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.INVALID_TRANSFER_AMOUNT.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.INVALID_TRANSFER_AMOUNT.getMessage());
                });
    }

    @Test
    void 출금계좌와_입금계좌가_같으면_SAME_ACCOUNT_TRANSFER_NOT_ALLOWED_예외를_던진다() {
        // Given
        TransferRequestDto requestDto = TransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .sendName("테스트송신자")
                .recvAccountNumber("1111-111-1111") // 출금 계좌와 동일한 계좌번호
                .recvBankCode(1)
                .recvName("테스트수신자")
                .amount(15000L)
                .build();
        Long userId = 2L;

        // When & Then
        assertThatThrownBy(() -> transferService.transfer(userId, requestDto, TransferMethod.GENERAL))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode())
                            .isEqualTo(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED.getCode());
                    assertThat(exception.getStatus().getMessage())
                            .isEqualTo(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED.getMessage());
                });
    }

    @Test
    void 블루투스_출금계좌와_입금계좌가_같으면_SAME_ACCOUNT_TRANSFER_NOT_ALLOWED_예외를_던진다() {
        // Given
        // FakeAccountServiceClient는 수신자 계좌번호를 항상 "1111-111-1112"로 반환
        // 출금 계좌도 동일하게 설정해서 예외 유도
        fakeAccountServiceClient = new FakeAccountServiceClient() {
            @Override
            public AccountResponseDto getAccountInfo(Long accountId, String userId) {
                return new AccountResponseDto("1111-111-1112"); // 입금 계좌와 동일하게 설정
            }
        };

        accountInfoResolver = new AccountInfoResolver(fakeAccountServiceClient);

        this.transferService = new TransferServiceImpl(
                transferValidator,
                accountInfoResolver,
                transferHistoryRecorder,
                notificationSender,
                fakeOpenBankingWebClient,
                fakeAccountServiceClient
        );

        BluetoothTransferRequestDto requestDto = BluetoothTransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .sendName("테스트송신자")
                .recvUserId(10L)
                .amount(15000L)
                .build();
        Long userId = 3L;

        // When & Then
        assertThatThrownBy(() -> transferService.bluetoothTransfer(userId, requestDto, TransferMethod.BLUETOOTH))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode())
                            .isEqualTo(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED.getCode());
                    assertThat(exception.getStatus().getMessage())
                            .isEqualTo(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED.getMessage());
                });
    }


    // ----------------------------------------------------------
    // 헬퍼 메서드
    // ----------------------------------------------------------
    private BluetoothTransferRequestDto 기본_블루투스_송금요청() {
        return BluetoothTransferRequestDto.builder()
                .sendAccountId(5L)
                .sendBankCode(1)
                .sendName("테스트송신자")
                .recvUserId(10L)
                .amount(15000L)
                .build();
    }
}