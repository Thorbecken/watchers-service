package com.watchers.helper;

import com.watchers.components.Computer;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.world.World;
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
        double time = stopWatch.getTotalTimeSeconds();
        if(time > 1d) {
            log.info("Processed " + message + " in " + time + " seconds.");
        } else {
            log.trace("Processed " + message + " in " + time + " seconds.");
        }
    }

    public static void processAndTime(Computer computer, WorldTaskDto worldTaskDto) {
        StopwatchTimer.start();
        computer.process(worldTaskDto);
        StopwatchTimer.stop(computer.getClass().getSimpleName());
    }

    public static Long processTimeAndCheckHeight(Computer computer, WorldTaskDto worldTaskDto, Long worldHeight){
        StopwatchTimer.start();
        computer.process(worldTaskDto);
        StopwatchTimer.stop(computer.getClass().getSimpleName());
        return checkWorldHeight(computer.getClass().getSimpleName(), worldHeight, worldTaskDto.getWorld());
    }


    private static Long checkWorldHeight(String processor, Long currentHeight, World world){
        Long newHeight = world.getWorldHeight() + world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof HotSpotCrystal)
                .map(pointOfInterest -> ((HotSpotCrystal) pointOfInterest))
                .mapToLong(HotSpotCrystal::getHeightBuildup)
                .sum();
        log.trace("HeightDeficit: " + world.getHeightDeficit() + " @" + processor);
        if(newHeight > currentHeight){
            log.error(processor + " changed the current height from " + currentHeight + " to " + newHeight);
            log.error(processor + " current height deficit from world " + world.getHeightDeficit());
        }
        return newHeight;
    }
}
