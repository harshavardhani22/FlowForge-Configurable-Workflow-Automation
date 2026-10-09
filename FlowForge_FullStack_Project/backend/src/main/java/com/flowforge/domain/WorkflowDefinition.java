package com.flowforge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WorkflowDefinition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(length=1200) private String description;
    @Column(nullable=false) private String createdBy = "demo";
    @Column(nullable=false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy="definition", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    @OrderColumn(name="stage_order") private List<WorkflowStage> stages = new ArrayList<>();

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){this.createdBy=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){this.createdAt=v;}
    public List<WorkflowStage> getStages(){return stages;} public void setStages(List<WorkflowStage> v){this.stages=v;}
}
