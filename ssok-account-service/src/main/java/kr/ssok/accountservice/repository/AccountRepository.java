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
     * 특정 사용자 ID에 해당하는 모든 LinkedAccount 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 LinkedAccount 리스트
     */
    List<LinkedAccount> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * 계좌 ID, 사용자 ID, 삭제되지 않은 상태 조건에 해당하는 LinkedAccount를 조회합니다.
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 조회된 LinkedAccount가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByAccountIdAndUserIdAndIsDeletedFalse(Long accountId, Long userId);

    /**
     * 특정 사용자 ID에 해당하며 주계좌(isPrimaryAccount=true)이고 삭제되지 않은 LinkedAccount를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 주계좌로 설정된 LinkedAccount가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(Long userId);

    /**
     * 계좌번호에 해당하는 LinkedAccount를 조회합니다.
     *
     * @param accountNumber 조회할 계좌번호
     * @return 조회된 LinkedAccount가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByAccountNumber(String accountNumber);

    /**
     * 삭제되지 않은 상태의 계좌번호에 해당하는 LinkedAccount를 조회합니다.
     *
     * @param accountNumber 계좌번호
     * @return 조회된 LinkedAccount가 존재하면 {@link Optional}로 반환, 존재하지 않으면 빈 Optional 반환
     */
    Optional<LinkedAccount> findByAccountNumberAndIsDeletedFalse(String accountNumber);
}
