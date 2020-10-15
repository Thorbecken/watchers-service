package com.watchers.scheduler.job;

import com.watchers.manager.TurnManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ProcessingUnitJob implements Job {

    @Autowired
    private TurnManager turnManager;

    public void execute(JobExecutionContext context) {

        log.trace("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        turnManager.processTurn();

        log.trace("Next turn scheduled @ {}", context.getNextFireTime());
    }
}