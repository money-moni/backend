package kr.ssok.accountservice.dto.response.transferservice;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 송금 서비스 등 내부 시스템 간 통신 시 계좌의 기본 정보를 전달하는 응답 DTO
 *
 * <p>계좌 ID, 사용자 ID, 계좌 번호를 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용이며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoResponseDto {
    private Long userId;
    private Long accountId;
    private String accountNumber;

    /**
     * LinkedAccount 엔티티를 AccountInfoResponseDto로 변환합니다.
     *
     * @param account 연동 계좌 엔티티
     * @return AccountInfoResponseDto 객체
     */
    public static AccountInfoResponseDto from(LinkedAccount account) {
        return AccountInfoResponseDto.builder()
                .userId(account.getUserId())
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .build();
    }
}
