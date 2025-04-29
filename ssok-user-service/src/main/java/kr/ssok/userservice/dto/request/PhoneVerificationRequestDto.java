package kr.ssok.userservice.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneVerificationRequestDto {
    private String phoneNumber;
    private String verificationCode;
}
