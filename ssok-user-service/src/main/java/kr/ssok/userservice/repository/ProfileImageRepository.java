package kr.ssok.userservice.repository;

import kr.ssok.userservice.entity.ProfileImage;
import kr.ssok.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByUser(User user);
    void deleteByUser(User user);
}
