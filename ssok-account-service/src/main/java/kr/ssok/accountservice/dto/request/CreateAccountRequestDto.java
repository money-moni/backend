package kr.ssok.accountservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 생성 요청 시 클라이언트로부터 전달받는 DTO
 *
 * <p>계좌 번호, 은행 코드, 계좌 타입 코드를 포함하며,
 * 이를 기반으로 새로운 연동 계좌를 생성할 때 사용됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDto {
    // 계좌 번호
    private String accountNumber;
    // 은행 코드 (BankCode Enum의 idx 값)
    private Long bankCode;
    // 계좌 타입 코드 (AccountTypeCode Enum의 idx 값)
    private Long accountTypeCode;
}
