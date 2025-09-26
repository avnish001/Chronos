package com.chronos.jobscheduler.scheduler;

import com.chronos.jobscheduler.model.Job;
import com.chronos.jobscheduler.model.JobExecution;
import com.chronos.jobscheduler.repository.JobExecutionRepository;
import com.chronos.jobscheduler.repository.JobRepository;
import com.chronos.jobscheduler.service.NotificationService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Date;

@Component
@DisallowConcurrentExecution
public class ExecutableJob extends QuartzJobBean {

    @Autowired private JobRepository jobRepository;
    @Autowired private JobExecutionRepository jobExecutionRepository;
    @Autowired private Scheduler scheduler;
    @Autowired private NotificationService notificationService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getMergedJobDataMap().getLong("jobId");
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;
        if ("CANCELLED".equalsIgnoreCase(job.getStatus())) return;

        JobExecution exec = new JobExecution();
        exec.setJobId(jobId);
        exec.setStartedAt(Instant.now());
        exec.setStatus("RUNNING");
        jobExecutionRepository.save(exec);
        try {
            // Simulated work: for HTTP_CALL we could call remote endpoint; here we simulate.
            Thread.sleep(800);
            // Simulate occasional failure if payload contains "fail"
            if (job.getPayload()!=null && job.getPayload().toLowerCase().contains("fail")) {
                throw new RuntimeException("Simulated job failure because payload asked to 'fail'");
            }
            exec.setFinishedAt(Instant.now());
            exec.setStatus("SUCCESS");
            exec.setLogs("Executed successfully (simulated).");
            job.setStatus("SUCCESS");
            job.setLastRunAt(Instant.now());
            job.setRetries(0);
            jobRepository.save(job);
        } catch (Exception e) {
            exec.setFinishedAt(Instant.now());
            exec.setStatus("FAILED");
            exec.setLogs("Exception: " + e.getMessage());
            job.setStatus("FAILED");
            int retries = job.getRetries() == 0 ? 1 : job.getRetries() + 1;
            job.setRetries(retries);
            jobRepository.save(job);

            // If we still have retries left, schedule a retry with exponential backoff
            if (retries <= job.getMaxRetries()) {
                long backoffSeconds = (long) Math.pow(2, retries); // 2^retries seconds
                try {
                    JobDetail detail = JobBuilder.newJob(ExecutableJob.class)
                        .withIdentity("job-retry-" + job.getId() + "-" + System.currentTimeMillis(), "jobs-retry")
                        .usingJobData("jobId", job.getId())
                        .build();
                    Trigger trg = TriggerBuilder.newTrigger()
                        .forJob(detail)
                        .startAt(new Date(System.currentTimeMillis() + backoffSeconds * 1000))
                        .build();
                    scheduler.scheduleJob(detail, trg);
                    // update nextRunAt for visibility
                    job.setNextRunAt(Instant.now().plusSeconds(backoffSeconds));
                    jobRepository.save(job);
                } catch (SchedulerException se) {
                    System.err.println("Failed to schedule retry: " + se.getMessage());
                }
            } else {
                // notify owner that job exceeded retries
                try {
                    // attempt to find owner's email (best-effort)
                    if (job.getOwnerId() != null) {
                        // repository access to find user email omitted to avoid circular dependency; notification can be manual configured
                        notificationService.sendFailureNotification("admin@example.com",
                            "Job " + job.getName() + " failed permanently",
                            "Job id " + job.getId() + " failed after " + retries + " attempts.");
                    }
                } catch (Exception n) {
                    System.err.println("Notification failed: " + n.getMessage());
                }
            }
        } finally {
            jobExecutionRepository.save(exec);
        }
    }
}
