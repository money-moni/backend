package kr.ssok.transferservice.service;

import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        when(transferHistoryRepository.findHistories(accountId, threeMonthsAgo))
                .thenReturn(List.of(
                        dummyTransferHistory(1L, TransferType.WITHDRAWAL),
                        dummyTransferHistory(2L, TransferType.DEPOSIT)
                ));

        // When
        List<TransferHistoryResponseDto> result = transferHistoryService.getTransferHistories(accountId);

        // Then
        assertThat(result).hasSize(2);
        // 1번 거래 내역
        assertThat(result.get(0).getTransferId()).isEqualTo(1L);
        assertThat(result.get(0).getTransferType()).isEqualTo("WITHDRAWAL");
        assertThat(result.get(1).getTransferId()).isEqualTo(2L);
        assertThat(result.get(1).getTransferType()).isEqualTo("DEPOSIT");

        verify(transferHistoryRepository, times(1)).findHistories(anyLong(), any(LocalDateTime.class));
    }

    private TransferHistory dummyTransferHistory(Long id, TransferType type) {
        return TransferHistory.builder()
                .id(id)
                .accountId(5L)
                .counterpartAccount("1111-1111-1111")
                .counterpartName("테스트상대")
                .transferType(type)
                .transferMoney(10000L)
                .currencyCode(CurrencyCode.KRW)
                .transferMethod(TransferMethod.GENERAL)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
