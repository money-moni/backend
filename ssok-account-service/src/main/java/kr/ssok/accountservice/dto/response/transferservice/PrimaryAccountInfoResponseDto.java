package kr.ssok.accountservice.dto.response.transferservice;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 송금 서비스 등 내부 시스템 간 통신 시 사용자 주계좌 정보를 제공하는 응답 DTO
 *
 * <p>계좌 ID, 계좌 번호, 은행 코드, 계좌명을 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용이며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryAccountInfoResponseDto {
    private Long accountId;
    private String accountNumber;
    private int bankCode;
    private String accountName;

    /**
     * LinkedAccount 엔티티와 사용자 이름을 기반으로 PrimaryAccountInfoResponseDto로 변환합니다.
     *
     * @param account 주계좌로 설정된 LinkedAccount 엔티티
     * @param username 사용자 이름 (계좌명으로 사용됨)
     * @return PrimaryAccountInfoResponseDto 객체
     */
    public static PrimaryAccountInfoResponseDto from(LinkedAccount account, String username) {
        return PrimaryAccountInfoResponseDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .bankCode(account.getBankCode().getIdx())
                .accountName(username)
                .build();
    }
}
