package com.flowforge.repo;
import com.flowforge.domain.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {}
