package com.chronos.jobscheduler.repository;

import com.chronos.jobscheduler.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByOwnerId(Long ownerId);
}
