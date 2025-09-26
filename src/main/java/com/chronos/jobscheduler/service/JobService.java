package com.chronos.jobscheduler.service;

import com.chronos.jobscheduler.model.Job;
import com.chronos.jobscheduler.model.JobExecution;
import com.chronos.jobscheduler.model.User;
import com.chronos.jobscheduler.repository.JobExecutionRepository;
import com.chronos.jobscheduler.repository.JobRepository;
import com.chronos.jobscheduler.repository.UserRepository;
import com.chronos.jobscheduler.scheduler.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.quartz.SchedulerException;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchedulerService schedulerService;

    // Create a new job
    public Job createJob(Job job, Authentication auth) throws SchedulerException {
        if (auth != null && auth.getName() != null) {
            Optional<User> u = userRepository.findByUsername(auth.getName());
            u.ifPresent(user -> job.setOwnerId(user.getId()));
        }
        job.setStatus("PENDING");
        Job saved = jobRepository.save(job);
        schedulerService.scheduleJob(saved);
        return saved;
    }

    // Get all jobs of a user
    public List<Job> getUserJobs(Long ownerId, Authentication auth) {
        Optional<User> u = userRepository.findByUsername(auth.getName());
        if (u.isPresent() && u.get().getId().equals(ownerId)) {
            return jobRepository.findByOwnerId(ownerId);
        }
        return List.of();
    }

    // Get job details
    public Job getJob(Long id, Authentication auth) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) return null;
        Optional<User> u = userRepository.findByUsername(auth.getName());
        return (u.isPresent() && u.get().getId().equals(job.getOwnerId())) ? job : null;
    }

    // Cancel a job
    public String cancelJob(Long id, Authentication auth) throws SchedulerException {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) return "not_found";
        Optional<User> u = userRepository.findByUsername(auth.getName());
        if (u.isPresent() && u.get().getId().equals(job.getOwnerId())) {
            job.setStatus("CANCELLED");
            jobRepository.save(job);
            schedulerService.deleteJob(id);
            return "cancelled";
        }
        return "forbidden";
    }

    // Reschedule a job
    public Job rescheduleJob(Long id, Job updated, Authentication auth) throws SchedulerException {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) return null;
        Optional<User> u = userRepository.findByUsername(auth.getName());
        if (u.isPresent() && u.get().getId().equals(job.getOwnerId())) {
            job.setCronExpression(updated.getCronExpression());
            job.setScheduleType(updated.getScheduleType());
            job.setScheduledTime(updated.getScheduledTime());
            Job saved = jobRepository.save(job);
            schedulerService.rescheduleJob(saved);
            return saved;
        }
        return null;
    }

    // Get job execution logs
    public List<JobExecution> getExecutions(Long id, Authentication auth) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) return List.of();
        Optional<User> u = userRepository.findByUsername(auth.getName());
        if (u.isPresent() && u.get().getId().equals(job.getOwnerId())) {
            return jobExecutionRepository.findByJobIdOrderByStartedAtDesc(id);
        }
        return List.of();
    }
}
