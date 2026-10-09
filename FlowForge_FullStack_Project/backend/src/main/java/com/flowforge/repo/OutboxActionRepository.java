package com.flowforge.repo;
import com.flowforge.domain.OutboxAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface OutboxActionRepository extends JpaRepository<OutboxAction, Long> {
 List<OutboxAction> findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc(String status, Instant now);
}
