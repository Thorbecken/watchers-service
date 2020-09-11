package com.watchers.scheduler;

import com.watchers.service.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisallowConcurrentExecution
public class SaveJob implements Job {

    @Autowired
    private WorldService worldService;

    public void execute(JobExecutionContext context) {

        log.info("Save job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        worldService.saveWorlds();

        log.info("Next save job scheduled @ {}", context.getNextFireTime());
    }
}