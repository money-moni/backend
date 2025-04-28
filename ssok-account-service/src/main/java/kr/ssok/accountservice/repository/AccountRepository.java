package kr.ssok.accountservice.repository;

import kr.ssok.accountservice.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * LinkedAccount 엔티티에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 *
 * <p>Spring Data JPA를 통해 기본적인 CRUD 기능을 제공합니다.</p>
 */
public interface AccountRepository extends JpaRepository<LinkedAccount, Long> {
    /**
     * 주어진 계좌 번호를 가진 LinkedAccount가 존재하는지 확인합니다.
     *
     * @param accountNumber 조회할 계좌 번호
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByAccountNumber(String accountNumber);
}
