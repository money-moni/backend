package kr.ssok.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AligoVerificationRequestDto {
    private String phoneNumber;
    private String verificationCode;
}
