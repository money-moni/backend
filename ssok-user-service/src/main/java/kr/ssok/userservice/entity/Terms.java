package kr.ssok.userservice.entity;

import jakarta.persistence.*;
import kr.ssok.common.entity.TimeStamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terms extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 약관 제목

    @Column(columnDefinition = "text")
    private String content; // 약관 내용 (html 형태로)
}
