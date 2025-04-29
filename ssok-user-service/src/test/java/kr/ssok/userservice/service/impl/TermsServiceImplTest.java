package kr.ssok.userservice.service.impl;

import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;
import kr.ssok.userservice.entity.Terms;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.TermsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TermsServiceImpl 클래스에 대한 단위 테스트
 * 약관 목록 조회 및 상세 조회 메서드에 대한 테스트를 포함합니다.
 */
@ExtendWith(MockitoExtension.class)
class TermsServiceImplTest {

    @InjectMocks
    private TermsServiceImpl termsService;

    @Mock
    private TermsRepository termsRepository;

    /**
     * 약관 목록 조회 기능에 대한 테스트
     * 여러 개의 약관이 있을 때 모든 약관이 올바르게 반환되고,
     * 각 약관의 ID와 제목이 올바르게 매핑되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 목록 조회 시 올바른 TermsListResponseDto 목록 반환 검증 테스트")
    void getTermsList_ReturnsCorrectDtoList() {
        // given
        List<Terms> termsList = Arrays.asList(
                Terms.builder().id(1L).title("이용약관").content("이용약관 내용").build(),
                Terms.builder().id(2L).title("개인정보처리방침").content("개인정보처리방침 내용").build(),
                Terms.builder().id(3L).title("위치정보 이용약관").content("위치정보 이용약관 내용").build()
        );
        
        when(termsRepository.findAll()).thenReturn(termsList);
        
        // when
        List<TermsListResponseDto> result = termsService.getTermsList();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        
        // 첫 번째 항목 검증
        assertThat(result.get(0).getTermsId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("이용약관");
        
        // 두 번째 항목 검증
        assertThat(result.get(1).getTermsId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("개인정보처리방침");
        
        // 세 번째 항목 검증
        assertThat(result.get(2).getTermsId()).isEqualTo(3L);
        assertThat(result.get(2).getTitle()).isEqualTo("위치정보 이용약관");
        
        // Repository 호출 검증
        verify(termsRepository).findAll();
    }
    
    /**
     * 약관이 없는 경우에 대한 테스트
     * 약관이 없을 때 빈 리스트가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관이 없을 경우 빈 목록 반환 검증 테스트")
    void getTermsList_ReturnsEmptyList_WhenNoTermsExist() {
        // given
        when(termsRepository.findAll()).thenReturn(new ArrayList<>());
        
        // when
        List<TermsListResponseDto> result = termsService.getTermsList();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        // Repository 호출 검증
        verify(termsRepository).findAll();
    }
    
    /**
     * 약관 목록 DTO 매핑에 대한 테스트
     * 엔티티에서 DTO로 변환될 때 필드 매핑이 정확하게 이루어지는지 검증합니다.
     * 특히 TermsListResponseDto에는 content 필드가 포함되지 않는지 확인합니다.
     */
    @Test
    @DisplayName("반환된 DTO의 id와 title 필드 매핑 검증 테스트")
    void getTermsList_CorrectlyMapsDtoFields() {
        // given
        Terms terms = Terms.builder()
                .id(1L)
                .title("이용약관")
                .content("이용약관 내용")
                .build();
        
        when(termsRepository.findAll()).thenReturn(List.of(terms));
        
        // when
        List<TermsListResponseDto> result = termsService.getTermsList();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        
        TermsListResponseDto dto = result.get(0);
        assertThat(dto.getTermsId()).isEqualTo(terms.getId());
        assertThat(dto.getTitle()).isEqualTo(terms.getTitle());
        
        // Content 필드가 DTO에 포함되지 않는지 검증
        // TermsListResponseDto에는 content 필드가 없으므로 직접 검증할 수 없음
        // 대신 DTO 클래스에 content 필드가 없음을 확인해야 함
        
        // Repository 호출 검증
        verify(termsRepository).findAll();
    }
    
    /**
     * 존재하는 약관 ID로 상세 조회 테스트
     * 유효한 ID로 약관을 조회할 때 올바른 상세 정보가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("존재하는 약관 ID로 조회 시 올바른 TermsDetailResponseDto 반환 검증 테스트")
    void getTermsDetail_ReturnsCorrectDto_WhenTermsExists() {
        // given
        Long termsId = 1L;
        Terms terms = Terms.builder()
                .id(termsId)
                .title("이용약관")
                .content("이용약관 내용")
                .build();
        
        when(termsRepository.findById(termsId)).thenReturn(Optional.of(terms));
        
        // when
        TermsDetailResponseDto result = termsService.getTermsDetail(termsId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTermsId()).isEqualTo(termsId);
        assertThat(result.getTitle()).isEqualTo("이용약관");
        assertThat(result.getContent()).isEqualTo("이용약관 내용");
        
        // Repository 호출 검증
        verify(termsRepository).findById(termsId);
    }
    
    /**
     * 존재하지 않는 약관 ID로 상세 조회 테스트
     * 존재하지 않는 ID로 조회 시 TERMS_NOT_FOUND 예외가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("존재하지 않는 약관 ID로 조회 시 적절한 예외 발생 테스트")
    void getTermsDetail_ThrowsException_WhenTermsDoesNotExist() {
        // given
        Long termsId = 999L;
        when(termsRepository.findById(termsId)).thenReturn(Optional.empty());
        
        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            termsService.getTermsDetail(termsId);
        });
        
        // 예외 메시지 및 상태 검증
        assertThat(exception.getStatus()).isEqualTo(UserResponseStatus.TERMS_NOT_FOUND);
        
        // Repository 호출 검증
        verify(termsRepository).findById(termsId);
    }
    
    /**
     * 약관 상세 DTO 매핑에 대한 테스트
     * 상세 정보 DTO에 ID, 제목, 내용이 모두 올바르게 매핑되는지 검증합니다.
     */
    @Test
    @DisplayName("반환된 DTO의 id, title, content 필드 매핑 검증 테스트")
    void getTermsDetail_CorrectlyMapsDtoFields() {
        // given
        Long termsId = 1L;
        Terms terms = Terms.builder()
                .id(termsId)
                .title("이용약관")
                .content("이용약관 상세 내용")
                .build();
        
        when(termsRepository.findById(termsId)).thenReturn(Optional.of(terms));
        
        // when
        TermsDetailResponseDto result = termsService.getTermsDetail(termsId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTermsId()).isEqualTo(terms.getId());
        assertThat(result.getTitle()).isEqualTo(terms.getTitle());
        assertThat(result.getContent()).isEqualTo(terms.getContent());
        
        // Repository 호출 검증
        verify(termsRepository).findById(termsId);
    }
}
