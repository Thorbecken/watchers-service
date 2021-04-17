package com.watchers.manager;

import com.watchers.components.cleaners.AnimalCleaner;
import com.watchers.components.cleaners.ContinentAfterCleaner;
import com.watchers.components.cleaners.WorldCleaner;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CleansingManager {
    private AnimalCleaner animalCleaner;
    private ContinentAfterCleaner continentAfterCleaner;
    private WorldCleaner worldCleanser;

    public void process(WorldTaskDto taskDto){
        worldCleanser.proces(taskDto);
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.start();
            continentAfterCleaner.process((ContinentalDriftTaskDto) taskDto);
            StopwatchTimer.stop("continentAfterCleaner");
            StopwatchTimer.start();
            animalCleaner.process((ContinentalDriftTaskDto) taskDto);
            StopwatchTimer.stop("animalCleaner");
        }
    }

}
