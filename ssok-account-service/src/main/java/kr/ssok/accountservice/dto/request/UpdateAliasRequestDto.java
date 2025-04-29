package kr.ssok.accountservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계좌 별명 수정 요청 시 클라이언트로부터 전달받는 DTO
 *
 * <p>계좌 별명을 포함하며,
 * 이를 기반으로 기존 연동 계좌의 별명을 수정할 때 사용됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAliasRequestDto {
    // 계좌 별명
    private String accountAlias;
}
