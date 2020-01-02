package com.watchers.scheduler;

import com.watchers.job.TurnJobService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TurnJob  implements Job {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TurnJobService turnJobService;

    public void execute(JobExecutionContext context) {

        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        turnJobService.executeSampleJob();

        logger.info("Next job scheduled @ {}", context.getNextFireTime());
    }
}