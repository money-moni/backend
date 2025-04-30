package kr.ssok.accountservice.dto.response.openbanking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 실명 조회 응답 DTO
 *
 * <p>계좌 번호와 은행 코드에 해당하는 예금주(사용자 이름)를 담습니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAccountOwnerResponseDto {
    // 사용자 이름
    private String username;
}
