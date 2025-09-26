package com.chronos.jobscheduler.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "job_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long jobId;
    private Instant startedAt;
    private Instant finishedAt;
    private String status;
    @Column(length = 3000)
    private String logs;
}
