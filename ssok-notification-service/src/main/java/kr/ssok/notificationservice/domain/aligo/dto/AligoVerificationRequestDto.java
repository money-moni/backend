package kr.ssok.notificationservice.domain.aligo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AligoVerificationRequestDto {
    private String phoneNumber;
    private String verificationCode;
}
