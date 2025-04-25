package kr.ssok.transferservice.service;

import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TransferService의 단위 테스트.
 * 외부 통신(FakeAccountServiceClient, FakeOpenBankingClient) 없이 비즈니스 로직만 검증
 * 모의 객체(Mock) 대신 페이크 객체(Fake)를 사용
 */
public class TransferServiceTest {

    @Test
    void 송금이_정상적으로_수행된다() {
        // given: 송금 요청 DTO
        TransferRequestDto dto = TransferRequestDto.builder()
                .sendAccountId(5L) // 출금 계좌 (본인 계좌)
                .sendBankCode(1) // 쏙 뱅크 은행 코드
                .recvAccountNumber("1111-111-1112") // 입금 계좌 (송금 대상)
                .recvBankCode(1) // 쏙 뱅크 은행 코드
                .amount(15000L) // 송금 금액
                .build();

        Long userIdFromHeader = 2L; // gateway로부터 헤더로 전달 받을 userId

        // 의존성 주입: 가짜 구현체로 대체
        TransferServiceImpl service = new TransferServiceImpl(
                new FakeAccountServiceClient(),
                new FakeOpenBankingClient(),
                new FakeTransferHistoryRepository()
        );

        // when: 송금 수행
        TransferResponseDto response = service.transfer(userIdFromHeader, dto);

        // then: 응답 메시지 및 코드 검증
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("송금에 성공했습니다.");
    }

    /**
     * Fake: 계좌 서비스 응답 Fake 객체
     * 응답 구조:
     * {
     *   "code": 200,
     *   "message": "계좌 번호 조회를 완료했습니다.",
     *   "result": {
     *       "userId": 2,
     *       "accountId": 5,
     *       "accountNumber": "1111-111-1111"
     *   }
     * }
     */
    private static class FakeAccountServiceClient implements AccountServiceClient {
        @Override
        public String getAccountNumber(Long accountId, Long userId) {
            // 계좌 Id와 userId가 확인하는 값과 동일하다면 (자세한 내부로직은 생략)
            if (accountId.equals(5L) && userId.equals(2L)) {
                return "1111-111-1111"; // 응답에서 추출될 값
            }
            throw new IllegalArgumentException("유효하지 않은 계좌 또는 사용자"); // 임시 예외 처리
        }
    }

    /**
     * Fake: 오픈뱅킹 송금 응답 Fake 객체
     * 응답 구조:
     * {
     *   "code": 200,
     *   "message": "송금에 성공했습니다.",
     *   "result": {}
     * }
     */
    private static class FakeOpenBankingClient implements OpenBankingClient {
        @Override
        public void sendTransferRequest(String sendAccountNumber, String sendBankCode,
                                        String recvAccountNumber, String recvBankCode, Long amount) {
            // 1. 출금 계좌번호 유효성 검증
            if (!"1111-111-1111".equals(sendAccountNumber)) {
                throw new RuntimeException("출금 계좌번호 오류");
            }

            // 2. 오픈뱅킹 응답 시뮬레이션
            OpenBankingResponse response = new OpenBankingResponse(200, "송금에 성공했습니다.");

            // 3. code가 200이 아니면 실패로 간주
            if (response.getCode() != 200) {
                throw new RuntimeException("오픈뱅킹 송금 실패: " + response.getMessage());
            }
        }

        /**
         * 테스트용 오픈뱅킹 응답 객체 (실제 HTTP 통신 대신 내부 객체로 흉내)
         */
        private static class OpenBankingResponse {
            private final int code;
            private final String message;

            public OpenBankingResponse(int code, String message) {
                this.code = code;
                this.message = message;
            }

            public int getCode() {
                return this.code;
            }

            public String getMessage() {
                return this.message;
            }
        }
    }

    /**
     * Fake: JPA 저장소를 Fake (DB 저장 대신 단순 반환)
     */
    private static class FakeTransferHistoryRepository implements TransferHistoryRepository {
        @Override
        public <S extends TransferHistory> S save(S entity) {
            // 그대로 반환
            return entity;
        }
    }
}