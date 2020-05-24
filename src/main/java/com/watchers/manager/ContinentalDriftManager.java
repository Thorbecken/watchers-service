package com.watchers.manager;

import com.watchers.components.continentaldrift.ContinentalDriftWorldAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftDirectionAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftTileAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftAdjuster;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContinentalDriftManager {

    private ContinentalDriftAdjuster continentalDriftAdjuster;
    private ContinentalDriftTileAdjuster continentalDriftTileAdjuster;
    private long heigtDivider;
    private ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;

    public ContinentalDriftManager(ContinentalDriftAdjuster continentalDriftAdjuster,
                                   ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster,
                                   ContinentalDriftTileAdjuster continentalDriftTileAdjuster,
                                   ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster,
                                   @Value("${watch.heightdivider}") long heigtDivider){
        this.continentalDriftAdjuster = continentalDriftAdjuster;
        this.continentalDriftTileAdjuster = continentalDriftTileAdjuster;
        this.heigtDivider = heigtDivider;
        this.continentalDriftDirectionAdjuster = continentalDriftDirectionAdjuster;
        this.continentalDriftWorldAdjuster = continentalDriftWorldAdjuster;
    }

    public void process(World world){
        ContinentalDriftTaskDto taskDto = setup(world);

        continentalDriftDirectionAdjuster.processContinentalDrift(world);
        continentalDriftAdjuster.calculateContinentalDrift(taskDto);
        continentalDriftTileAdjuster.process(taskDto);
        continentalDriftWorldAdjuster.process(taskDto);

        log.info("Proccesed a continentaldrift for world id: " + world.getId());
    }

    private ContinentalDriftTaskDto setup(World world) {
        ContinentalDriftTaskDto taskDto = new ContinentalDriftTaskDto();
        taskDto.setWorld(world);
        taskDto.setHeightDivider(heigtDivider);

        return taskDto;
    }

}
