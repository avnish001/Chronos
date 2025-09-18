package com.chronos.jobscheduler.controller;

import com.chronos.jobscheduler.model.Job;
import com.chronos.jobscheduler.model.JobExecution;
import com.chronos.jobscheduler.service.JobService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Protected Job management endpoints.
 * All endpoints require a valid JWT (except if you configure otherwise).
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, Authentication auth) {
        try {
            Job created = jobService.createJob(job, auth);
            return ResponseEntity.ok(created);
        } catch (SchedulerException se) {
            return ResponseEntity.status(500).body(Map.of("error", "scheduler_error", "message", se.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "message", e.getMessage()));
        }
    }

    @GetMapping("/user/{ownerId}")
    public ResponseEntity<?> getByOwner(@PathVariable Long ownerId, Authentication auth) {
        List<Job> jobs = jobService.getUserJobs(ownerId, auth);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Authentication auth) {
        Job job = jobService.getJob(id, auth);
        if (job == null) return ResponseEntity.status(404).body(Map.of("error", "not_found"));
        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        try {
            String res = jobService.cancelJob(id, auth);
            switch (res) {
                case "deleted":
                case "cancelled":
                    return ResponseEntity.ok(Map.of("status", res));
                case "forbidden":
                    return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
                case "not_found":
                default:
                    return ResponseEntity.status(404).body(Map.of("error", "not_found"));
            }
        } catch (SchedulerException se) {
            return ResponseEntity.status(500).body(Map.of("error", "scheduler_error", "message", se.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        try {
            String res = jobService.cancelJob(id, auth);
            if ("cancelled".equals(res)) return ResponseEntity.ok(Map.of("status", "cancelled"));
            if ("forbidden".equals(res)) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
            return ResponseEntity.status(404).body(Map.of("error", "not_found"));
        } catch (SchedulerException se) {
            return ResponseEntity.status(500).body(Map.of("error", "scheduler_error", "message", se.getMessage()));
        }
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(@PathVariable Long id, @RequestBody Job updated, Authentication auth) {
        try {
            Job saved = jobService.rescheduleJob(id, updated, auth);
            if (saved == null) return ResponseEntity.status(403).body(Map.of("error", "forbidden_or_not_found"));
            return ResponseEntity.ok(saved);
        } catch (SchedulerException se) {
            return ResponseEntity.status(500).body(Map.of("error", "scheduler_error", "message", se.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<?> executions(@PathVariable Long id, Authentication auth) {
        List<JobExecution> execs = jobService.getExecutions(id, auth);
        return ResponseEntity.ok(execs);
    }
}
