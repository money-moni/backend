package kr.ssok.userservice.repository;

import kr.ssok.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    void deleteByPhoneNumber(String phoneNumber);
}
