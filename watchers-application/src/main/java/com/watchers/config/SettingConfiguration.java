package com.watchers.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Data
@Service
public class SettingConfiguration {

    // Service configuration
    private boolean persistent;
    private int processingTimer;
    private int turnTimer;
    private int continentalshiftTimer;
    private int saveTimer;
    private String directory;
    private String prefix;

    public SettingConfiguration(
            Environment environment,

            @Value("${watch.startup.persistent}") boolean persistent,
            @Value("${watch.startup.processingTimer}") int processingTimer,
            @Value("${watch.startup.turnTimer}") int turnTimer,
            @Value("${watch.startup.continentalshiftTimer}") int continentalshiftTimer,
            @Value("${watch.startup.saveTimer}") int saveTimer,
            @Value("${watch.startup.prefix:}") String prefix){
        this.persistent = persistent;
        this.processingTimer = processingTimer;
        this.turnTimer = turnTimer;
        this.continentalshiftTimer = continentalshiftTimer;
        this.saveTimer = saveTimer;
        this.directory = environment.getProperty("WATCHERS_SAVE_PATH");
        this.prefix = prefix;
    }
}
