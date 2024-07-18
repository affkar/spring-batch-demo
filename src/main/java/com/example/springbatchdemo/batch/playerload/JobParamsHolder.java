package com.example.springbatchdemo.batch.playerload;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Service;

@Service
public class JobParamsHolder implements JobExecutionListener {

    private JobExecution jobExecution;

    public void beforeJob(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public JobParameters getJobParams() {
        return this.jobExecution.getJobParameters();
    }

}
