package com.flowforge.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class WorkflowTask {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private WorkflowInstance instance;
    @Column(nullable=false) private String stageName;
    @Column(nullable=false) private String requiredRole;
    @Column(nullable=false) private String status = "PENDING";
    private String assignedTo;
    private String lastComment;
    @Column(nullable=false) private Instant createdAt = Instant.now();
    private Instant completedAt;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public WorkflowInstance getInstance(){return instance;} public void setInstance(WorkflowInstance v){instance=v;}
    public String getStageName(){return stageName;} public void setStageName(String v){stageName=v;}
    public String getRequiredRole(){return requiredRole;} public void setRequiredRole(String v){requiredRole=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getAssignedTo(){return assignedTo;} public void setAssignedTo(String v){assignedTo=v;}
    public String getLastComment(){return lastComment;} public void setLastComment(String v){lastComment=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getCompletedAt(){return completedAt;} public void setCompletedAt(Instant v){completedAt=v;}
}
