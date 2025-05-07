package kr.ssok.accountservice.dto.response.transferservice;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 송금 서비스 등 내부 시스템 간 통신 시 계좌 ID만 전달할 때 사용하는 응답 DTO
 *
 * <p>계좌 ID를 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용이며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountIdResponseDto {
    private Long accountId;

    /**
     * LinkedAccount 엔티티를 AccountIdResponseDto로 변환합니다.
     *
     * @param account 연동 계좌 엔티티
     * @return AccountIdResponseDto 객체
     */
    public static AccountIdResponseDto from(LinkedAccount account) {
        return AccountIdResponseDto.builder()
                .accountId(account.getAccountId())
                .build();
    }
}
