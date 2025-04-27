package kr.ssok.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponseDto {
    private Long userId;
    private String username;
    private String phoneNumber;
    private String accountNumber;
    private String hashedUserCode;
}