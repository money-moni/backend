package kr.ssok.transferservice.service;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.impl.TransferHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 송금 이력 조회 서비스 단위 테스트
 */
public class TransferHistoryServiceTest {

    private AccountServiceClient accountServiceClient;
    private TransferHistoryService transferHistoryService;
    private TransferHistoryRepository transferHistoryRepository;

    @BeforeEach
    void setUp() {
        accountServiceClient = mock(AccountServiceClient.class);
        transferHistoryRepository = mock(TransferHistoryRepository.class);
        transferHistoryService = new TransferHistoryServiceImpl(accountServiceClient, transferHistoryRepository);
    }

    @Test
    void 계좌ID로_3개월_이내_송금내역을_조회한다() {
        // Given
        Long accountId = 5L;
        List<TransferHistory> dummyHistories = List.of(
                dummyTransferHistory(1L, TransferType.WITHDRAWAL, LocalDateTime.now().minusDays(10)),
                dummyTransferHistory(2L, TransferType.DEPOSIT, LocalDateTime.now().minusDays(20)),
                dummyTransferHistory(3L, TransferType.WITHDRAWAL, LocalDateTime.now().minusDays(50))
        );

        when(transferHistoryRepository.findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(dummyHistories);

        // When
        List<TransferHistoryResponseDto> result = transferHistoryService.getTransferHistories(accountId);

        // Then
        assertThat(result).hasSize(3);

        assertThat(result.get(0).getTransferId()).isEqualTo(1L);
        assertThat(result.get(0).getTransferType()).isEqualTo(TransferType.WITHDRAWAL);

        assertThat(result.get(1).getTransferId()).isEqualTo(2L);
        assertThat(result.get(1).getTransferType()).isEqualTo(TransferType.DEPOSIT);

        assertThat(result.get(2).getTransferId()).isEqualTo(3L);
        assertThat(result.get(2).getTransferType()).isEqualTo(TransferType.WITHDRAWAL);

        // ArgumentCaptor로 실제 전달된 LocalDateTime 검증
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(transferHistoryRepository, times(1)).findByAccountIdAndCreatedAtAfterOrderByCreatedAtDesc(eq(accountId), captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        assertThat(capturedTime).isBefore(LocalDateTime.now()); // 3개월 전 시간이어야 함
    }

    @Test
    void 계좌ID가_null이면_INVALID_ACCOUNT_ID_예외를_던진다() {
        // Given
        Long accountId = null;

        // When & Then
        assertThatThrownBy(() -> transferHistoryService.getTransferHistories(accountId))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.INVALID_ACCOUNT_ID.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.INVALID_ACCOUNT_ID.getMessage());
                });

        // Repository는 호출되면 안 됨
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 유저ID로_최근송금상대를_정상조회한다() {
        // Given
        Long userId = 1L;
        List<Long> accountIds = List.of(10L, 20L);

        // 1. 계좌 서비스 모킹
        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공",
                        List.of(new AccountIdResponseDto(10L, 1L), new AccountIdResponseDto(20L, 2L))));

        // 2. 송금 상대 조회 모킹
        List<TransferCounterpartResponseDto> dummyResult = List.of(
                new TransferCounterpartResponseDto("최지훈", "123-456-789", LocalDateTime.now()),
                new TransferCounterpartResponseDto("홍길동", "987-654-321", LocalDateTime.now())
        );
        when(transferHistoryRepository.findRecentCounterparts(accountIds)).thenReturn(dummyResult);

        // When
        List<TransferCounterpartResponseDto> results = transferHistoryService.getRecentCounterparts(userId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCounterpartName()).isEqualTo("최지훈");
        assertThat(results.get(1).getCounterpartName()).isEqualTo("홍길동");

        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verify(transferHistoryRepository, times(1)).findRecentCounterparts(accountIds);
    }

    @Test
    void 최근송금상대조회_중복제거_입금제외_블루투스제외_검증() {
        // Given
        Long userId = 1L;
        List<Long> accountIds = List.of(10L, 20L);

        // 1. 계좌 서비스 모킹
        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공",
                        List.of(new AccountIdResponseDto(10L, 1L), new AccountIdResponseDto(20L, 2L))));

        // 2. 송금 이력 더미 데이터
        List<TransferHistory> dummyHistories = List.of(
                // 출금 + 일반 송금 - OK (1번째)
                dummyTransferHistory(1L, 10L, TransferType.WITHDRAWAL, TransferMethod.GENERAL, "최지훈", "123-456-789", LocalDateTime.now().minusDays(1)),
                // 출금 + 블루투스 송금 - 제외
                dummyTransferHistory(2L, 11L, TransferType.WITHDRAWAL, TransferMethod.BLUETOOTH, "홍길동", "987-654-321", LocalDateTime.now().minusDays(2)),
                // 입금 + 일반 송금 - 제외
                dummyTransferHistory(3L, 20L, TransferType.DEPOSIT, TransferMethod.GENERAL, "김영희", "222-333-444", LocalDateTime.now().minusDays(3)),
                // 출금 + 일반 송금 - OK (2번째)
                dummyTransferHistory(3L, 20L, TransferType.WITHDRAWAL, TransferMethod.GENERAL, "김영희", "222-333-444", LocalDateTime.now().minusDays(3)),
                // 출금 + 일반 송금 + 동일 상대 계좌 (중복) - 무시
                dummyTransferHistory(4L, 10L, TransferType.WITHDRAWAL, TransferMethod.GENERAL, "최지훈", "123-456-789", LocalDateTime.now().minusDays(4))
        );

        // 3. 송금 상대 조회 모킹 (Repository는 실제 필터링해서 반환한다고 가정)
        when(transferHistoryRepository.findRecentCounterparts(accountIds))
                .thenReturn(dummyHistories.stream()
                        // 1) accountIds에 포함된 것만
                        .filter(h -> accountIds.contains(h.getAccountId()))
                        // 2) 출금(WITHDRAWAL)만
                        .filter(h -> h.getTransferType() == TransferType.WITHDRAWAL)
                        // 3) 일반 송금(GENERAL)만
                        .filter(h -> h.getTransferMethod() == TransferMethod.GENERAL)
                        // 4) TransferCounterpartResponseDto로 변환
                        .map(h -> new TransferCounterpartResponseDto(
                                h.getCounterpartName(),
                                h.getCounterpartAccount(),
                                h.getCreatedAt()
                        ))
                        // 5) counterpartAccountNumber 기준으로 가장 최신만 남기기 (중복 제거)
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(
                                        TransferCounterpartResponseDto::getCounterpartAccountNumber, // 계좌번호 기준 중복 제거
                                        dto -> dto,
                                        (dto1, dto2) -> dto1, // 최신순으로 정렬됐다고 가정하고 첫번째(dto1) 유지
                                        LinkedHashMap::new
                                ),
                                map -> map.values().stream().toList()
                        ))
                );

        // When
        List<TransferCounterpartResponseDto> results = transferHistoryService.getRecentCounterparts(userId);

        // Then
        assertThat(results).hasSize(2); // 중복 제거되고 2개만 남아야 함

        TransferCounterpartResponseDto counterpart1 = results.get(0);
        assertThat(counterpart1.getCounterpartName()).isEqualTo("최지훈");
        assertThat(counterpart1.getCounterpartAccountNumber()).isEqualTo("123-456-789");

        TransferCounterpartResponseDto counterpart2 = results.get(1);
        assertThat(counterpart2.getCounterpartName()).isEqualTo("김영희");
        assertThat(counterpart2.getCounterpartAccountNumber()).isEqualTo("222-333-444");

        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verify(transferHistoryRepository, times(1)).findRecentCounterparts(accountIds);
    }

    @Test
    void 유저ID가_null이면_INVALID_USER_ID_예외를_던진다() {
        // Given
        Long userId = null;

        // When & Then
        assertThatThrownBy(() -> transferHistoryService.getRecentCounterparts(userId))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.INVALID_USER_ID.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.INVALID_USER_ID.getMessage());
                });

        // Repository, AccountServiceClient 둘 다 호출되면 안 됨
        verifyNoInteractions(accountServiceClient);
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID_조회결과가_null이면_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공", null)); // 계좌 리스트 null

        // When
        List<TransferCounterpartResponseDto> results = transferHistoryService.getRecentCounterparts(userId);

        // Then
        assertThat(results).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID_조회결과가_빈리스트이면_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공", List.of())); // 계좌 리스트 비어 있음

        // When
        List<TransferCounterpartResponseDto> results = transferHistoryService.getRecentCounterparts(userId);

        // Then
        assertThat(results).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID_조회에_실패하면_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(false, 4001, "계좌 조회 실패", null)); // 실패 응답

        // When
        List<TransferCounterpartResponseDto> results = transferHistoryService.getRecentCounterparts(userId);

        // Then
        assertThat(results).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }

    /**
     * 테스트용 더미 송금 이력 생성 (단순 버전)
     */
    private TransferHistory dummyTransferHistory(Long id, TransferType type, LocalDateTime createdAt) {
        return dummyTransferHistory(id, 5L, type, TransferMethod.GENERAL, "테스트상대", "1111-1111-1111", createdAt);
    }

    /**
     * 테스트용 더미 송금 이력 생성 (확장 버전)
     */
    private TransferHistory dummyTransferHistory(Long id, Long accountId, TransferType type, TransferMethod method, String name, String account, LocalDateTime createdAt) {
        return TransferHistory.builder()
                .id(id)
                .accountId(accountId)
                .transferType(type)
                .transferMethod(method)
                .counterpartName(name)
                .counterpartAccount(account)
                .transferMoney(10000L)
                .currencyCode(CurrencyCode.KRW)
                .createdAt(createdAt)
                .build();
    }

    @Test
    void 유저ID로_최근송금이력_3건을_정상조회한다() {
        // Given
        Long userId = 1L;
        List<AccountIdResponseDto> accountIdDtos = List.of(
                new AccountIdResponseDto(100L, userId),
                new AccountIdResponseDto(200L, userId)
        );

        List<Long> accountIds = accountIdDtos.stream().map(AccountIdResponseDto::getAccountId).toList();

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공", accountIdDtos));

        List<TransferHistory> dummyHistories = List.of(
                dummyTransferHistory(1L, 100L, TransferType.WITHDRAWAL, TransferMethod.GENERAL, "A", "111", LocalDateTime.now().minusDays(1)),
                dummyTransferHistory(2L, 100L, TransferType.DEPOSIT, TransferMethod.GENERAL, "B", "222", LocalDateTime.now().minusDays(2)),
                dummyTransferHistory(3L, 200L, TransferType.WITHDRAWAL, TransferMethod.BLUETOOTH, "C", "333", LocalDateTime.now().minusDays(3))
        );

        when(transferHistoryRepository.findTop3ByAccountIdInOrderByCreatedAtDesc(accountIds)).thenReturn(dummyHistories);

        // When
        var result = transferHistoryService.getRecentHistories(userId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTransferId()).isEqualTo(1L);
        assertThat(result.get(0).getTransferType()).isEqualTo(TransferType.WITHDRAWAL);
        assertThat(result.get(0).getCounterpartName()).isEqualTo("A");

        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verify(transferHistoryRepository, times(1)).findTop3ByAccountIdInOrderByCreatedAtDesc(accountIds);
    }

    @Test
    void 유저ID가_null이면_최근송금조회시_INVALID_USER_ID_예외를_던진다() {
        // Given
        Long userId = null;

        // When & Then
        assertThatThrownBy(() -> transferHistoryService.getRecentHistories(userId))
                .isInstanceOf(TransferException.class)
                .satisfies(ex -> {
                    TransferException exception = (TransferException) ex;
                    assertThat(exception.getStatus().getCode()).isEqualTo(TransferResponseStatus.INVALID_USER_ID.getCode());
                    assertThat(exception.getStatus().getMessage()).isEqualTo(TransferResponseStatus.INVALID_USER_ID.getMessage());
                });

        verifyNoInteractions(accountServiceClient);
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID조회응답이_실패일경우_최근송금조회는_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(false, 4001, "계좌 조회 실패", null));

        // When
        var result = transferHistoryService.getRecentHistories(userId);

        // Then
        assertThat(result).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID조회결과가_null이면_최근송금조회는_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공", null));

        // When
        var result = transferHistoryService.getRecentHistories(userId);

        // Then
        assertThat(result).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }

    @Test
    void 계좌ID조회결과가_빈리스트이면_최근송금조회는_빈리스트를_반환한다() {
        // Given
        Long userId = 1L;

        when(accountServiceClient.getAccountIdsByUserId(userId.toString()))
                .thenReturn(new BaseResponse<>(true, 2000, "성공", List.of()));

        // When
        var result = transferHistoryService.getRecentHistories(userId);

        // Then
        assertThat(result).isEmpty();
        verify(accountServiceClient, times(1)).getAccountIdsByUserId(userId.toString());
        verifyNoInteractions(transferHistoryRepository);
    }
}
