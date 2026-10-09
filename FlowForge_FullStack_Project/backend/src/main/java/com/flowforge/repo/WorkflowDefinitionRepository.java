package com.flowforge.repo;
import com.flowforge.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {}
