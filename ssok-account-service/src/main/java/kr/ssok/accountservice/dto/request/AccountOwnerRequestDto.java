package kr.ssok.accountservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 연동 계좌 실명 확인 요청을 위한 DTO
 *
 * <p>클라이언트가 입력한 계좌번호와 은행 코드 정보를 포함하며,
 * 오픈뱅킹 서버에 실명 조회 요청을 전달하기 위해 사용됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountOwnerRequestDto {
    // 계좌번호
    private String accountNumber;
    // 은행 코드 (BankCode Enum의 idx 값)
    private int bankCode;
}
