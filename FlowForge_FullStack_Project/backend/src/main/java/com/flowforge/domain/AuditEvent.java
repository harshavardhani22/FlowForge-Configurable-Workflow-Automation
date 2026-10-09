package com.flowforge.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AuditEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private WorkflowInstance instance;
    private Long taskId;
    @Column(nullable=false) private String eventType;
    @Column(nullable=false) private String actor;
    @Column(length=1600) private String details;
    @Column(nullable=false) private Instant occurredAt = Instant.now();
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public WorkflowInstance getInstance(){return instance;} public void setInstance(WorkflowInstance v){instance=v;}
    public Long getTaskId(){return taskId;} public void setTaskId(Long v){taskId=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getActor(){return actor;} public void setActor(String v){actor=v;}
    public String getDetails(){return details;} public void setDetails(String v){details=v;}
    public Instant getOccurredAt(){return occurredAt;} public void setOccurredAt(Instant v){occurredAt=v;}
}
