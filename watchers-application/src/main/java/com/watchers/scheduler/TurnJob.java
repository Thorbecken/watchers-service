package com.watchers.scheduler;

import com.watchers.service.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class TurnJob  implements Job {

    @Autowired
    private WorldService worldService;

    public void execute(JobExecutionContext context) {

        log.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        worldService.executeTurn();

        log.info("Next turn scheduled @ {}", context.getNextFireTime());
    }
}