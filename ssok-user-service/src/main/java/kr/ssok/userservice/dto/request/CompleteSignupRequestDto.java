package kr.ssok.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSignupRequestDto {
    
    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "휴대폰 번호 형식이 올바르지 않습니다")
    private String phoneNumber;

    @NotBlank(message = "이름은 필수입니다")
    @Pattern(regexp = "^[가-힣a-zA-Z]{2,10}$", message = "이름은 2-10자의 한글 또는 영문이어야 합니다")
    private String username;

    @NotBlank(message = "생년월일은 필수입니다")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자여야 합니다 (YYYYMMDD)")
    private String birthDate;

    @NotNull(message = "PIN 코드는 필수입니다")
    @Pattern(regexp = "^\\d{6}$", message = "PIN 코드는 6자리 숫자여야 합니다")
    private String pinCode;
}
