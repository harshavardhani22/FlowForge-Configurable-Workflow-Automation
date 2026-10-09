package com.flowforge.repo;
import com.flowforge.domain.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
 List<WorkflowTask> findByStatusOrderByCreatedAtDesc(String status);
 List<WorkflowTask> findAllByOrderByCreatedAtDesc();
}
