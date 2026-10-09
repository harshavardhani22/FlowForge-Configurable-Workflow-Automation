package com.flowforge.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class WorkflowStage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="definition_id", nullable=false) private WorkflowDefinition definition;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String requiredRole = "USER";
    private String onApprove;
    private String onReject;
    private String onReturn;
    private String onEscalate;
    private String conditionKey;
    private String conditionOperator;
    private String conditionValue;
    private String conditionTarget;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public WorkflowDefinition getDefinition(){return definition;} public void setDefinition(WorkflowDefinition v){definition=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getRequiredRole(){return requiredRole;} public void setRequiredRole(String v){requiredRole=v;}
    public String getOnApprove(){return onApprove;} public void setOnApprove(String v){onApprove=v;}
    public String getOnReject(){return onReject;} public void setOnReject(String v){onReject=v;}
    public String getOnReturn(){return onReturn;} public void setOnReturn(String v){onReturn=v;}
    public String getOnEscalate(){return onEscalate;} public void setOnEscalate(String v){onEscalate=v;}
    public String getConditionKey(){return conditionKey;} public void setConditionKey(String v){conditionKey=v;}
    public String getConditionOperator(){return conditionOperator;} public void setConditionOperator(String v){conditionOperator=v;}
    public String getConditionValue(){return conditionValue;} public void setConditionValue(String v){conditionValue=v;}
    public String getConditionTarget(){return conditionTarget;} public void setConditionTarget(String v){conditionTarget=v;}
}
