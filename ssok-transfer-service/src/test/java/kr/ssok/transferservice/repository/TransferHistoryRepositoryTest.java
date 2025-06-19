package kr.ssok.transferservice.repository;

import kr.ssok.transferservice.config.QueryDSLConfig;
import kr.ssok.transferservice.dto.response.TransferCounterpartResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.enums.BankCode;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * TransferHistoryRepository 단위 테스트
 * - QueryDSL을 통한 송금 상대 조회(findRecentCounterparts) 기능 검증
 */
@DataJpaTest
@Import(QueryDSLConfig.class)
class TransferHistoryRepositoryTest {

    @Autowired
    private TransferHistoryRepository transferHistoryRepository;

    @Test
    void 계좌ID목록으로_최근송금상대를_중복제거하여_조회한다() {
        // Given: 5건의 송금 이력을 저장
        // - 동일 상대방(홍길동) 계좌에 2번 송금하여 중복 케이스 구성
        // - Bluetooth 송금, 입금 내역은 조회 대상이 아님
        TransferHistory history1 = createTransferHistory(100L, "홍길동", "123-456", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now().minusDays(1));
        TransferHistory history2 = createTransferHistory(300L, "박민수", "999-999", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now().minusDays(2));
        TransferHistory history3 = createTransferHistory(100L, "홍길동", "123-456", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now().minusDays(3)); // 중복 송금
        TransferHistory history4 = createTransferHistory(200L, "이철수", "345-678", TransferType.WITHDRAWAL, TransferMethod.BLUETOOTH, LocalDateTime.now().minusDays(1)); // Bluetooth 송금
        TransferHistory history5 = createTransferHistory(300L, "박민수", "999-999", TransferType.DEPOSIT, TransferMethod.GENERAL, LocalDateTime.now().minusDays(1)); // 입금 내역

        transferHistoryRepository.saveAll(List.of(history1, history2, history3, history4, history5));

        List<Long> accountIds = List.of(100L, 200L, 300L);

        // When: 송금 상대 목록 조회
        List<TransferCounterpartResponseDto> results = transferHistoryRepository.findRecentCounterparts(accountIds);

        // Then: 블루투스 송금/입금 제외, 중복 제거 후 2건만 조회되어야 함
        assertThat(results).hasSize(2);

        assertThat(results.get(0).getCounterpartName()).isEqualTo("홍길동");
        assertThat(results.get(0).getCounterpartAccountNumber()).isEqualTo("123-456");

        assertThat(results.get(1).getCounterpartName()).isEqualTo("박민수");
        assertThat(results.get(1).getCounterpartAccountNumber()).isEqualTo("999-999");
    }

    @Test
    void 계좌ID목록이_비어있으면_빈리스트를_반환한다() {
        // Given: 빈 계좌 ID 목록
        List<Long> emptyAccountIds = Collections.emptyList();

        // When: 송금 상대 조회
        List<TransferCounterpartResponseDto> results = transferHistoryRepository.findRecentCounterparts(emptyAccountIds);

        // Then: 결과는 비어 있어야 한다
        assertThat(results).isEmpty();
    }

    @Test
    void 계좌ID목록에_해당하는송금내역이없으면_빈리스트를_반환한다() {
        // Given: DB에 존재하지 않는 계좌 ID 목록
        List<Long> nonexistentAccountIds = List.of(9999L, 8888L);

        // When: 송금 상대 조회
        List<TransferCounterpartResponseDto> results = transferHistoryRepository.findRecentCounterparts(nonexistentAccountIds);

        // Then: 결과는 비어 있어야 한다
        assertThat(results).isEmpty();
    }

    @Test
    void 계좌ID목록이_null이면_예외없이_빈리스트를_반환한다() {
        // Given: null 계좌 ID 목록
        List<Long> nullAccountIds = null;

        // When & Then: NullPointerException 없이 정상적으로 빈 리스트 반환
        assertDoesNotThrow(() -> {
            List<TransferCounterpartResponseDto> results = transferHistoryRepository.findRecentCounterparts(nullAccountIds);
            assertThat(results).isEmpty();
        });
    }

    /**
     * 테스트용 송금 이력 객체를 생성하는 유틸 메서드
     */
    private TransferHistory createTransferHistory(Long accountId, String counterpartName, String counterpartAccount,
                                                  TransferType transferType, TransferMethod transferMethod, LocalDateTime createdAt) {
        return TransferHistory.builder()
                .accountId(accountId)
                .counterpartName(counterpartName)
                .counterpartAccount(counterpartAccount)
                .counterpartBankCode(BankCode.SSOK_BANK)
                .transferType(transferType)
                .transferMethod(transferMethod)
                .transferMoney(10000L)
                .currencyCode(CurrencyCode.KRW)
                .createdAt(createdAt)
                .build();
    }

    @Test
    void 계좌ID목록으로_최근송금이력_3건을_조회한다() throws InterruptedException {
        // Given: 4건의 송금 이력을 저장 (계좌ID: 101)
        TransferHistory history1 = createTransferHistory(101L, "김철수", "100-100", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now());
        TransferHistory history2 = createTransferHistory(101L, "김철수", "100-100", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now());
        TransferHistory history3 = createTransferHistory(101L, "김철수", "100-100", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now());
        TransferHistory history4 = createTransferHistory(101L, "김철수", "100-100", TransferType.WITHDRAWAL, TransferMethod.GENERAL, LocalDateTime.now());

        transferHistoryRepository.saveAll(List.of(history1, history2, history3, history4));

        List<Long> accountIds = List.of(101L);

        // When: 최근 송금 이력 3건 조회
        List<TransferHistory> result = transferHistoryRepository
                .findTop3ByAccountIdInOrderByCreatedAtDesc(accountIds);

        // Then: 최신 순으로 3건만 반환되어야 함
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(TransferHistory::getId)
                .containsExactly(history4.getId(), history3.getId(), history2.getId());
    }
}
