package kr.ssok.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsDetailResponseDto {
    private Long termsId;
    private String title;
    private String content;
}
