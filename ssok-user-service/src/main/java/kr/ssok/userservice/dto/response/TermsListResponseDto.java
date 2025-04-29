package kr.ssok.userservice.dto.response;

import kr.ssok.userservice.entity.Terms;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsListResponseDto {
    private Long termsId;
    private String title;

    public static TermsListResponseDto from(Terms terms) {
        return TermsListResponseDto.builder()
                .termsId(terms.getId())
                .title(terms.getTitle())
                .build();
    }
}
