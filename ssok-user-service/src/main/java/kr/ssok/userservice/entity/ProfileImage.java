package kr.ssok.userservice.entity;

import jakarta.persistence.*;
import kr.ssok.common.entity.TimeStamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProfileImage extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String storedFilename; // e7b8f3a2-9df4-456e.png

    private String url; // s3 저장경로

    private String contentType; // image/jpeg
}
