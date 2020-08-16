package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContinentalDriftManager {

    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private TileDefined tileDefined;
    private ErosionAdjuster erosionAdjuster;

    private long heigtDivider;
    private int minimumContinents;

    public ContinentalDriftManager(ContinentalDriftPredicter continentalDriftPredicter,
                                   ContinentalDriftDirectionChanger continentalDriftDirectionChanger,
                                   ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer,
                                   ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster,
                                   ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner,
                                   TileDefined tileDefined,
                                   ErosionAdjuster erosionAdjuster,
                                   @Value("${watch.heightdivider}") long heigtDivider,
                                   @Value("${watch.minContinents}") int minimumContinents){
        this.continentalDriftPredicter = continentalDriftPredicter;
        this.continentalDriftTileChangeComputer = continentalDriftTileChangeComputer;
        this.continentalDriftDirectionChanger = continentalDriftDirectionChanger;
        this.continentalDriftWorldAdjuster = continentalDriftWorldAdjuster;
        this.continentalDriftNewTileAssigner = continentalDriftNewTileAssigner;
        this.tileDefined = tileDefined;
        this.erosionAdjuster = erosionAdjuster;

        this.heigtDivider = heigtDivider;
        this.minimumContinents = minimumContinents;
    }

    public ContinentalDriftTaskDto process(World world){
        ContinentalDriftTaskDto taskDto = setup(world);
       continentalDriftDirectionChanger.process(world);
        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
        continentalDriftWorldAdjuster.process(taskDto);
        erosionAdjuster.process(taskDto);
        tileDefined.process(taskDto.getWorld());

        log.info("Proccesed a continentaldrift for world id: " + world.getId());

        return taskDto;
    }

    private ContinentalDriftTaskDto setup(World world) {
        ContinentalDriftTaskDto taskDto = new ContinentalDriftTaskDto();
        taskDto.setWorld(world);
        taskDto.setHeightDivider(heigtDivider);
        taskDto.setMinContinents(minimumContinents);

        return taskDto;
    }

}
