package com.flowforge.service;

import com.flowforge.domain.*;
import com.flowforge.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class WorkflowService {
    private final WorkflowDefinitionRepository definitions;
    private final WorkflowInstanceRepository instances;
    private final WorkflowTaskRepository tasks;
    private final AuditEventRepository audits;
    private final OutboxActionRepository outbox;
    private final Set<String> idempotencyKeys = Collections.synchronizedSet(new HashSet<>());

    public WorkflowService(WorkflowDefinitionRepository d, WorkflowInstanceRepository i, WorkflowTaskRepository t,
                           AuditEventRepository a, OutboxActionRepository o) {
        definitions=d; instances=i; tasks=t; audits=a; outbox=o;
    }

    @Transactional
    public WorkflowDefinition createDefinition(Map<String,Object> body) {
        String name = Objects.toString(body.get("name"), "").trim();
        if (name.isBlank()) throw new IllegalArgumentException("Definition name is required");
        WorkflowDefinition d = new WorkflowDefinition();
        d.setName(name); d.setDescription(Objects.toString(body.get("description"), ""));
        Object raw = body.get("stages");
        if (!(raw instanceof List<?> stageList) || stageList.isEmpty()) throw new IllegalArgumentException("At least one stage is required");
        for (Object obj : stageList) {
            if (!(obj instanceof Map<?,?> map)) continue;
            WorkflowStage s = new WorkflowStage();
            s.setDefinition(d);
            s.setName(Objects.toString(map.get("name"), "").trim());
            if (s.getName().isBlank()) throw new IllegalArgumentException("Every stage needs a name");
            s.setRequiredRole(Objects.toString(map.get("requiredRole"), "USER").toUpperCase(Locale.ROOT));
            s.setOnApprove(Objects.toString(map.get("onApprove"), ""));
            s.setOnReject(Objects.toString(map.get("onReject"), "REJECTED"));
            s.setOnReturn(Objects.toString(map.get("onReturn"), "RETURNED"));
            s.setOnEscalate(Objects.toString(map.get("onEscalate"), "ADMIN"));
            s.setConditionKey(Objects.toString(map.get("conditionKey"), ""));
            s.setConditionOperator(Objects.toString(map.get("conditionOperator"), ""));
            s.setConditionValue(Objects.toString(map.get("conditionValue"), ""));
            s.setConditionTarget(Objects.toString(map.get("conditionTarget"), ""));
            d.getStages().add(s);
        }
        return definitions.save(d);
    }

    @Transactional
    public WorkflowInstance startInstance(Map<String,Object> body) {
        Long defId = Long.valueOf(Objects.toString(body.get("definitionId"), "0"));
        WorkflowDefinition d = definitions.findById(defId).orElseThrow(() -> new IllegalArgumentException("Workflow definition not found"));
        if (d.getStages().isEmpty()) throw new IllegalArgumentException("Workflow has no stages");
        WorkflowInstance i = new WorkflowInstance();
        i.setDefinition(d); i.setTitle(Objects.toString(body.get("title"), d.getName()));
        Object context = body.get("context");
        if (context instanceof Map<?,?> m) {
            Map<String,Object> ctx = new LinkedHashMap<>();
            m.forEach((k,v) -> ctx.put(String.valueOf(k),v));
            i.setContext(ctx);
        }
        i.setCurrentStage(d.getStages().get(0).getName());
        i = instances.save(i);
        createTask(i, d.getStages().get(0));
        audit(i, null, "WORKFLOW_STARTED", "demo", "Started workflow at " + i.getCurrentStage());
        enqueue("workflow-started-" + i.getId(), "WORKFLOW_STARTED", "instanceId=" + i.getId());
        return i;
    }

    @Transactional
    public WorkflowTask act(Long taskId, Map<String,Object> body, String authenticatedUser) {
        String key = Objects.toString(body.get("idempotencyKey"), "").trim();
        if (key.isBlank()) throw new IllegalArgumentException("idempotencyKey is required");
        if (!idempotencyKeys.add(key)) {
            return tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        }
        WorkflowTask task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!"PENDING".equals(task.getStatus())) throw new IllegalStateException("Task is no longer pending");
        WorkflowInstance i = task.getInstance();
        String action = Objects.toString(body.get("action"), "").toUpperCase(Locale.ROOT);
        String comment = Objects.toString(body.get("comment"), "");
        String actor = authenticatedUser == null ? "demo" : authenticatedUser;
        task.setLastComment(comment);
        task.setCompletedAt(Instant.now());
        task.setStatus(action.equals("RETURN") ? "RETURNED" : action.equals("ESCALATE") ? "ESCALATED" : action.equals("APPROVE") ? "APPROVED" : action.equals("REJECT") ? "REJECTED" : "INVALID");
        if ("INVALID".equals(task.getStatus())) throw new IllegalArgumentException("Action must be APPROVE, REJECT, RETURN, or ESCALATE");
        tasks.save(task);
        WorkflowStage stage = i.getDefinition().getStages().stream().filter(s -> s.getName().equals(task.getStageName())).findFirst()
            .orElseThrow(() -> new IllegalStateException("Stage not found"));
        String next = switch (action) {
            case "APPROVE" -> routeApproved(stage, i.getContext());
            case "REJECT" -> stage.getOnReject();
            case "RETURN" -> stage.getOnReturn();
            case "ESCALATE" -> stage.getOnEscalate();
            default -> "";
        };
        audit(i, task.getId(), "TASK_" + action, actor, comment);
        transition(i, next, actor);
        enqueue("task-action-" + key, "TASK_ACTION", "taskId=" + task.getId() + ";action=" + action);
        return task;
    }

    private String routeApproved(WorkflowStage stage, Map<String,Object> context) {
        if (stage.getConditionKey() != null && !stage.getConditionKey().isBlank()
            && stage.getConditionTarget() != null && !stage.getConditionTarget().isBlank()
            && conditionMatches(context.get(stage.getConditionKey()), stage.getConditionOperator(), stage.getConditionValue())) {
            return stage.getConditionTarget();
        }
        return stage.getOnApprove();
    }

    private boolean conditionMatches(Object actual, String op, String expected) {
        if (actual == null) return false;
        String operator = Objects.toString(op, "EQ").toUpperCase(Locale.ROOT);
        if ("CONTAINS".equals(operator)) return actual.toString().toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
        if ("GT".equals(operator) || "GTE".equals(operator)) {
            try { double a=Double.parseDouble(actual.toString()), e=Double.parseDouble(expected); return "GT".equals(operator) ? a>e : a>=e; }
            catch (NumberFormatException ignored) { return false; }
        }
        return "NE".equals(operator) ? !actual.toString().equalsIgnoreCase(expected) : actual.toString().equalsIgnoreCase(expected);
    }

    private void transition(WorkflowInstance i, String target, String actor) {
        i.setUpdatedAt(Instant.now());
        if (target == null || target.isBlank() || "APPROVED".equalsIgnoreCase(target)) {
            i.setStatus("APPROVED"); i.setCurrentStage("Completed");
            instances.save(i); audit(i, null, "WORKFLOW_COMPLETED", actor, "All approval stages completed"); return;
        }
        if ("REJECTED".equalsIgnoreCase(target)) {
            i.setStatus("REJECTED"); i.setCurrentStage("Rejected");
            instances.save(i); audit(i, null, "WORKFLOW_REJECTED", actor, "Workflow rejected"); return;
        }
        if ("RETURNED".equalsIgnoreCase(target)) {
            i.setStatus("RETURNED"); i.setCurrentStage("Returned to requester");
            instances.save(i); audit(i, null, "WORKFLOW_RETURNED", actor, "Workflow returned to requester"); return;
        }
        if ("ADMIN".equalsIgnoreCase(target)) {
            WorkflowStage adminStage = new WorkflowStage(); adminStage.setName("Admin escalation"); adminStage.setRequiredRole("ADMIN");
            i.setCurrentStage(adminStage.getName()); instances.save(i); createTask(i, adminStage);
            audit(i, null, "WORKFLOW_ESCALATED", actor, "Escalated to ADMIN"); return;
        }
        WorkflowStage next = i.getDefinition().getStages().stream().filter(s -> s.getName().equals(target)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown next stage: " + target));
        i.setStatus("RUNNING"); i.setCurrentStage(next.getName()); instances.save(i); createTask(i, next);
        audit(i, null, "STAGE_CHANGED", actor, "Moved to " + next.getName());
    }

    private void createTask(WorkflowInstance i, WorkflowStage s) {
        WorkflowTask t = new WorkflowTask(); t.setInstance(i); t.setStageName(s.getName()); t.setRequiredRole(s.getRequiredRole()); tasks.save(t);
    }
    private void audit(WorkflowInstance i, Long taskId, String type, String actor, String details) {
        AuditEvent e = new AuditEvent(); e.setInstance(i); e.setTaskId(taskId); e.setEventType(type); e.setActor(actor); e.setDetails(details); audits.save(e);
    }
    private void enqueue(String key, String type, String payload) {
        OutboxAction o = new OutboxAction(); o.setDedupeKey(key); o.setActionType(type); o.setPayload(payload);
        try { outbox.save(o); } catch (Exception ignored) { /* unique dedupe key: already queued */ }
    }
}
