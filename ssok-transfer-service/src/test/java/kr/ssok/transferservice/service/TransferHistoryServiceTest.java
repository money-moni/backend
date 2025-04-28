package kr.ssok.transferservice.service;

import kr.ssok.transferservice.dto.response.TransferHistoryResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.impl.TransferHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 송금 이력 조회 서비스 단위 테스트
 */
public class TransferHistoryServiceTest {

    private TransferHistoryService transferHistoryService;
    private TransferHistoryRepository transferHistoryRepository;

    @BeforeEach
    void setUp() {
        transferHistoryRepository = mock(TransferHistoryRepository.class);
        transferHistoryService = new TransferHistoryServiceImpl(transferHistoryRepository);
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

    /**
     * 테스트용 더미 송금 이력 생성
     *
     * @param id 송금 ID
     * @param type 송금 타입
     * @param createdAt 생성 시각
     * @return TransferHistory 객체
     */
    private TransferHistory dummyTransferHistory(Long id, TransferType type, LocalDateTime createdAt) {
        return TransferHistory.builder()
                .id(id)
                .accountId(5L)
                .counterpartAccount("1111-1111-1111")
                .counterpartName("테스트상대")
                .transferType(type)
                .transferMoney(10000L)
                .currencyCode(CurrencyCode.KRW)
                .transferMethod(TransferMethod.GENERAL)
                .createdAt(createdAt)
                .build();
    }
}
