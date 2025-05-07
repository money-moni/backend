package kr.ssok.accountservice.dto.response.userservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 사용자 정보 조회 요청 시, 유저 서비스 서버로부터 전달받는 응답 DTO
 *
 * <p>사용자의 이름과 전화번호를 포함하며, 오픈뱅킹 전체 계좌 조회 요청 등에 사용됩니다.</p>
 *
 * <p>이 객체는 읽기 전용이며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private String username;
    private String phoneNumber;
}
