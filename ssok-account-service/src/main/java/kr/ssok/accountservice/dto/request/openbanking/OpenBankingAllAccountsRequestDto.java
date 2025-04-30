package kr.ssok.accountservice.dto.request.openbanking;

import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈뱅킹 서버에 전체 계좌 목록 조회 요청 시 사용되는 요청 DTO
 *
 * <p>사용자의 이름과 전화번호를 포함하며,
 * 해당 사용자의 전체 계좌 목록을 조회할 수 있도록 구성됩니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingAllAccountsRequestDto {
    // 사용자 이름
    private String username;
    // 전화번호
    private String phoneNumber;

    /**
     * userInfoResponse를 OpenBankingAllAccountsRequestDto로 변환합니다.
     *
     * @param userInfoResponse 유저 정보 DTO
     * @return OpenBankingAllAccountsRequestDto 객체
     */
    public static OpenBankingAllAccountsRequestDto from(UserInfoResponseDto userInfoResponse) {
        return OpenBankingAllAccountsRequestDto.builder()
                .username(userInfoResponse.getUsername())
                .phoneNumber(userInfoResponse.getPhoneNumber())
                .build();
    }
}
