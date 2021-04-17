package com.watchers.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public class StopwatchTimer {
    static StopWatch stopWatch;

    public static void start(){
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    public static void stop(String message){
        stopWatch.stop();
        log.debug("Processed " + message + " in " + stopWatch.getTotalTimeSeconds() + " seconds.");
    }

}
