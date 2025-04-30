package kr.ssok.accountservice.dto.response.userservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private String username;
    private String phoneNumber;
}
