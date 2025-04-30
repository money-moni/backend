package kr.ssok.accountservice.dto.response.openbanking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 전체 계좌 목록 조회 응답 DTO
 *
 * <p>사용자에게 귀속된 전체 계좌 목록 중 하나를 나타냅니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAllAccountsResponseDto {
    // 은행 코드 (BankCode Enum의 idx 값)
    private int bankCode;
    private String accountNumber;
    private int accountTypeCode;
}
