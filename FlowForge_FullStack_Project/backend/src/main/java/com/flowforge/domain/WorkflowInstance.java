package com.flowforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class WorkflowInstance {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private WorkflowDefinition definition;
    @Column(nullable=false) private String title;
    @Column(nullable=false) private String status = "RUNNING";
    private String currentStage;
    @Column(nullable=false) private String startedBy = "demo";
    @Column(nullable=false) private Instant createdAt = Instant.now();
    @Column(nullable=false) private Instant updatedAt = Instant.now();
    @Convert(converter=JsonMapConverter.class) @Column(columnDefinition="text") private Map<String,Object> context = new LinkedHashMap<>();

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public WorkflowDefinition getDefinition(){return definition;} public void setDefinition(WorkflowDefinition v){definition=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getCurrentStage(){return currentStage;} public void setCurrentStage(String v){currentStage=v;}
    public String getStartedBy(){return startedBy;} public void setStartedBy(String v){startedBy=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
    public Map<String,Object> getContext(){return context;} public void setContext(Map<String,Object> v){context=v;}
}
