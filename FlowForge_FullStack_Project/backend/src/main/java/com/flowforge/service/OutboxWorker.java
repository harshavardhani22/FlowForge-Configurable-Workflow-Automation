package com.flowforge.service;

import com.flowforge.domain.OutboxAction;
import com.flowforge.repo.OutboxActionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class OutboxWorker {
    private final OutboxActionRepository repository;
    public OutboxWorker(OutboxActionRepository repository) { this.repository = repository; }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processDueActions() {
        for (OutboxAction action : repository.findTop20ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc("PENDING", Instant.now())) {
            try {
                // Starter hook: replace with notification/webhook delivery. Dedupe key makes this action uniquely addressable.
                action.setStatus("COMPLETED");
                action.setLastError(null);
            } catch (Exception ex) {
                int attempts = action.getAttempts() + 1;
                action.setAttempts(attempts);
                action.setLastError(ex.getMessage());
                if (attempts >= 5) action.setStatus("DEAD");
                else action.setNextAttemptAt(Instant.now().plusSeconds((long)Math.pow(2, attempts) * 10));
            }
            repository.save(action);
        }
    }
}
