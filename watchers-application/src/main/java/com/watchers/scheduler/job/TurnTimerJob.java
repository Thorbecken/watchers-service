package com.watchers.scheduler.job;

import com.watchers.manager.TurnManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class TurnTimerJob implements Job {

    @Autowired
    private TurnManager turnManager;

    public void execute(JobExecutionContext context) {

        log.trace("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        turnManager.queInTurn();

        log.trace("Next turn scheduled @ {}", context.getNextFireTime());
    }
}