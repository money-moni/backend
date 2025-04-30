package kr.ssok.accountservice.dto.response;

import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import lombok.Builder;
import lombok.Getter;

/**
 * 계좌 실명 조회 결과 DTO
 *
 * <p>오픈뱅킹 서버로부터 받은 예금주 이름과 계좌 번호를 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
public class AccountOwnerResponseDto {
    // 사용자 이름
    private String username;
    // 계좌번호
    private String accountNumber;

    /**
     * OpenBankingAccountOwnerResponseDto를 AccountOwnerResponseDto로 변환합니다.
     *
     * @param openBankingAccountOwnerResponse 오픈뱅킹 실명 조회 응답 DTO
     * @param accountNumber 계좌 번호
     * @return AccountOwnerResponseDto 객체
     */
    public static AccountOwnerResponseDto from(OpenBankingAccountOwnerResponseDto openBankingAccountOwnerResponse, String accountNumber) {
        return AccountOwnerResponseDto.builder()
                .username(openBankingAccountOwnerResponse.getUsername())
                .accountNumber(accountNumber)
                .build();
    }
}
