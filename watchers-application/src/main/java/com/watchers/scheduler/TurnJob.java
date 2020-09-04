package com.watchers.scheduler;

import com.watchers.service.WorldService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class TurnJob  implements Job {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WorldService worldService;

    public void execute(JobExecutionContext context) {

        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        worldService.executeTurn();

        logger.info("Next turn scheduled @ {}", context.getNextFireTime());
    }
}