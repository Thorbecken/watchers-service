package com.watchers.manager;

import com.watchers.components.continentaldrift.ContinentalDriftWorldAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftDirectionAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftTileAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftAdjuster;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContinentalDriftManager {

    private ContinentalDriftAdjuster continentalDriftAdjuster;
    private ContinentalDriftTileAdjuster continentalDriftTileAdjuster;
    private long heigtDivider;
    private ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;

    public ContinentalDriftManager(ContinentalDriftAdjuster continentalDriftAdjuster,
                                   ContinentalDriftTileAdjuster continentalDriftTileAdjuster,
                                   @Value("${watch.heightdivider}") long heigtDivider,
                                   ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster,
                                   ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster){
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
    }

    private ContinentalDriftTaskDto setup(World world) {
        ContinentalDriftTaskDto taskDto = new ContinentalDriftTaskDto();
        taskDto.setWorld(world);
        taskDto.setHeightDivider(heigtDivider);

        return taskDto;
    }

}
