package com.flowforge.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class OutboxAction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String dedupeKey;
    @Column(nullable=false) private String actionType;
    @Column(nullable=false) private String status = "PENDING";
    @Column(nullable=false) private int attempts = 0;
    @Column(length=1200) private String payload;
    private String lastError;
    @Column(nullable=false) private Instant nextAttemptAt = Instant.now();
    @Column(nullable=false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getDedupeKey(){return dedupeKey;} public void setDedupeKey(String v){dedupeKey=v;}
    public String getActionType(){return actionType;} public void setActionType(String v){actionType=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;}
    public String getPayload(){return payload;} public void setPayload(String v){payload=v;}
    public String getLastError(){return lastError;} public void setLastError(String v){lastError=v;}
    public Instant getNextAttemptAt(){return nextAttemptAt;} public void setNextAttemptAt(Instant v){nextAttemptAt=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
}
