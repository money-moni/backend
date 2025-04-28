package kr.ssok.userservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerificationRequestDto {
    private String phoneNumber;
    private String verificationCode;
}
