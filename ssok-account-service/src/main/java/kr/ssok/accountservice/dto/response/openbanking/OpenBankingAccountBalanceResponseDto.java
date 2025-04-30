package kr.ssok.accountservice.dto.response.openbanking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 계좌 잔액 응답 DTO
 *
 * <p>오픈뱅킹 서버로부터 받은 계좌 잔액 정보를 담습니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAccountBalanceResponseDto {
    // 계좌 잔액
    private Long balance;
}
