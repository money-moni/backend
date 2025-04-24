package kr.ssok.transferservice.repository;

import kr.ssok.transferservice.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

}
