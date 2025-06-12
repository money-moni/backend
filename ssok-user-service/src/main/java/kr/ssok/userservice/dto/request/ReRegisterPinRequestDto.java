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
public class ReRegisterPinRequestDto {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotBlank(message = "PIN 코드는 필수입니다")
    @Pattern(regexp = "^\\d{6}$", message = "PIN 코드는 6자리 숫자여야 합니다")
    private String pinCode;
}
