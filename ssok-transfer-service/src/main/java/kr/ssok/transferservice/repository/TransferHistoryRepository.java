package kr.ssok.transferservice.repository;

import kr.ssok.transferservice.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {

}
