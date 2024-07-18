package com.example.springbatchdemo.batch.common;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Service;

@Service
public class JobExecutionContextHolder implements JobExecutionListener {

    private JobExecution jobExecution;

    public void beforeJob(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public ExecutionContext getExecutionContext() {
        return this.jobExecution.getExecutionContext();
    }

}
