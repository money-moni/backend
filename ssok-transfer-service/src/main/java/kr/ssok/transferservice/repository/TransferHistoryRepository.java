package kr.ssok.transferservice.repository;

import kr.ssok.transferservice.entity.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {

}
