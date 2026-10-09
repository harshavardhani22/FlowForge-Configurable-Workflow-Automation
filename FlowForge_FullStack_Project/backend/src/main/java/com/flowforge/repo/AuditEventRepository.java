package com.flowforge.repo;
import com.flowforge.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
 List<AuditEvent> findByInstanceIdOrderByOccurredAtAsc(Long instanceId);
}
