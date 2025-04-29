package kr.ssok.userservice.repository;

import kr.ssok.userservice.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
