package kr.ssok.userservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    
    @NotNull(message = "사용자 ID가 null 입니다.")
    private Long userId;
    
    @NotNull(message = "PIN 코드를 입력해 주세요.")
    private Integer pinCode;
}
