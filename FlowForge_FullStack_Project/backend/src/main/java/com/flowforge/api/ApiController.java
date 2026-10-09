package com.flowforge.api;

import com.flowforge.domain.*;
import com.flowforge.repo.*;
import com.flowforge.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final WorkflowDefinitionRepository definitions;
    private final WorkflowInstanceRepository instances;
    private final WorkflowTaskRepository tasks;
    private final AuditEventRepository audits;
    private final WorkflowService service;

    public ApiController(WorkflowDefinitionRepository d, WorkflowInstanceRepository i, WorkflowTaskRepository t,
                         AuditEventRepository a, WorkflowService s) {
        definitions=d; instances=i; tasks=t; audits=a; service=s;
    }

    @GetMapping("/health") public Map<String,Object> health(){return Map.of("status","UP","service","FlowForge API");}
    @GetMapping("/definitions") public List<WorkflowDefinition> definitions(){return definitions.findAll();}
    @PostMapping("/definitions") @ResponseStatus(HttpStatus.CREATED)
    public WorkflowDefinition createDefinition(@RequestBody Map<String,Object> body) {
        try { return service.createDefinition(body); } catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage()); }
    }
    @GetMapping("/instances") public List<WorkflowInstance> instances(){return instances.findAll();}
    @PostMapping("/instances") @ResponseStatus(HttpStatus.CREATED)
    public WorkflowInstance start(@RequestBody Map<String,Object> body) {
        try { return service.startInstance(body); } catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage()); }
    }
    @GetMapping("/instances/{id}") public WorkflowInstance instance(@PathVariable Long id) {
        return instances.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Instance not found"));
    }
    @GetMapping("/instances/{id}/timeline") public List<AuditEvent> timeline(@PathVariable Long id) {
        if (!instances.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Instance not found");
        return audits.findByInstanceIdOrderByOccurredAtAsc(id);
    }
    @GetMapping("/tasks") public List<WorkflowTask> tasks(@RequestParam(required=false) String status) {
        return status == null ? tasks.findAllByOrderByCreatedAtDesc() : tasks.findByStatusOrderByCreatedAtDesc(status.toUpperCase(Locale.ROOT));
    }
    @PostMapping("/tasks/{id}/actions") public WorkflowTask action(@PathVariable Long id, @RequestBody Map<String,Object> body, Authentication auth) {
        try { return service.act(id, body, auth == null ? "demo" : auth.getName()); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage()); }
        catch (IllegalStateException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,e.getMessage()); }
    }
}
