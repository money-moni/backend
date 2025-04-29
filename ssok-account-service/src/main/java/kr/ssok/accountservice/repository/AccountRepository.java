package kr.ssok.accountservice.repository;

import kr.ssok.accountservice.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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

    /**
     * 특정 사용자 ID에 해당하는 모든 LinkedAccount 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 LinkedAccount 리스트
     */
    List<LinkedAccount> findByUserId(Long userId);

    /**
     * 특정 계좌 ID와 사용자 ID에 해당하는 LinkedAccount를 조회합니다.
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 조회된 LinkedAccount가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByAccountIdAndUserId(Long accountId, Long userId);

    /**
     * 특정 사용자 ID에 해당하는 주계좌(Primary Account)를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 주계좌가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByUserIdAndIsPrimaryAccountTrue(Long userId);
}
