package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContinentalDriftManager {

    private ContinentalDriftAdjuster continentalDriftAdjuster;
    private ContinentalDriftTileAdjuster continentalDriftTileAdjuster;
    private ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private TileDefined tileDefined;
    private ErosionAdjuster erosionAdjuster;

    private long heigtDivider;
    private int minimumContinents;

    public ContinentalDriftManager(ContinentalDriftAdjuster continentalDriftAdjuster,
                                   ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster,
                                   ContinentalDriftTileAdjuster continentalDriftTileAdjuster,
                                   ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster,
                                   ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner,
                                   TileDefined tileDefined,
                                   ErosionAdjuster erosionAdjuster,
                                   @Value("${watch.heightdivider}") long heigtDivider,
                                   @Value("${watch.minContinents}") int minimumContinents){
        this.continentalDriftAdjuster = continentalDriftAdjuster;
        this.continentalDriftTileAdjuster = continentalDriftTileAdjuster;
        this.continentalDriftDirectionAdjuster = continentalDriftDirectionAdjuster;
        this.continentalDriftWorldAdjuster = continentalDriftWorldAdjuster;
        this.continentalDriftNewTileAssigner = continentalDriftNewTileAssigner;
        this.tileDefined = tileDefined;
        this.erosionAdjuster = erosionAdjuster;

        this.heigtDivider = heigtDivider;
        this.minimumContinents = minimumContinents;
    }

    public void process(World world){
        ContinentalDriftTaskDto taskDto = setup(world);

        continentalDriftDirectionAdjuster.processContinentalDrift(world);
        continentalDriftAdjuster.process(taskDto);
        continentalDriftTileAdjuster.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
        continentalDriftWorldAdjuster.process(taskDto);
        erosionAdjuster.process(taskDto);
        tileDefined.process(taskDto.getWorld());

        log.info("Proccesed a continentaldrift for world id: " + world.getId());
    }

    private ContinentalDriftTaskDto setup(World world) {
        ContinentalDriftTaskDto taskDto = new ContinentalDriftTaskDto();
        taskDto.setWorld(world);
        taskDto.setHeightDivider(heigtDivider);
        taskDto.setMinContinents(minimumContinents);

        return taskDto;
    }

}
