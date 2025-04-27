package kr.ssok.accountservice.repository;

import kr.ssok.accountservice.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<LinkedAccount, Long> {
    boolean existsByAccountNumber(String accountNumber);
}
