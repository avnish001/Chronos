package com.chronos.jobscheduler.scheduler;

import com.chronos.jobscheduler.model.Job;
import com.chronos.jobscheduler.repository.JobRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
public class SchedulerService {
    @Autowired private Scheduler scheduler;
    @Autowired private JobRepository jobRepository;

    public void scheduleJob(Job job) throws SchedulerException {
        JobDetail detail = JobBuilder.newJob(ExecutableJob.class)
            .withIdentity("job-" + job.getId(), "jobs")
            .usingJobData("jobId", job.getId())
            .build();

        Trigger trigger;
        if ("ONE_TIME".equalsIgnoreCase(job.getScheduleType()) && job.getScheduledTime()!=null) {
            Date d = Date.from(job.getScheduledTime().atZone(ZoneId.systemDefault()).toInstant());
            trigger = TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("trg-" + job.getId(), "jobs")
                .startAt(d)
                .build();
        } else {
            // assume CRON
            trigger = TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("trg-" + job.getId(), "jobs")
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .build();
        }
        scheduler.scheduleJob(detail, trigger);
    }

    public void deleteJob(Long jobId) throws SchedulerException {
        scheduler.deleteJob(new JobKey("job-" + jobId, "jobs"));
    }

    public void rescheduleJob(Job job) throws SchedulerException {
        // delete existing and schedule again
        deleteJob(job.getId());
        scheduleJob(job);
    }
}
