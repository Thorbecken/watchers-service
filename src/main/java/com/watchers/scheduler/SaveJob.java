package com.watchers.scheduler;

import com.watchers.service.SaveJobService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class SaveJob implements Job {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SaveJobService saveJobService;

    public void execute(JobExecutionContext context) {

        logger.info("Save job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        saveJobService.executeSave();

        logger.info("Next save job scheduled @ {}", context.getNextFireTime());
    }
}