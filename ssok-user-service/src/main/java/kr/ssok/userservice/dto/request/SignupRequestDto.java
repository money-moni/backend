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
public class SignupRequestDto {
    @NotBlank(message = "사용자 이름을 입력해 주세요.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "사용자 이름은 한글 또는 영문 대소문자만 포함할 수 있습니다.")
    private String username; // 회원명

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Pattern(regexp = "^[0-9]{12,13}$", message = "전화번호는 하이픈을 포함한 10~11자리 숫자여야 합니다.")
    private String phoneNumber; // 회원 전화번호

    @NotNull(message = "생년월일을 입력해 주세요.")
    private String birthDate; // 생년월일

    /* 성별 안쓰게 되서 주석처리 */
//    @NotNull(message = "주민등록번호 뒷자리 첫번째값이 null 입니다.")
//    private String gender;

    @NotNull(message = "pin번호 입력이 null입니다.")
    private int pinCode;
}
