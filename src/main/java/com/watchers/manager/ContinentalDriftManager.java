package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
        long initialHeight = world.getHeightDeficit();
        continentalDriftDirectionChanger.process(world);
        log.info("post directionChanger height: " + (worldHeight(world) + initialHeight));

        continentalDriftPredicter.process(taskDto);
        log.info("post driftdirection height: "+ (worldHeight(world) + initialHeight));
        log.info("height in new layout: " + (newMapHeight(taskDto) + initialHeight));

        continentalDriftTileChangeComputer.process(taskDto);
        log.info("post changeComputer height: "+ (worldHeight(world) + world.getHeightDeficit()));
        log.info("height in new changes: " + (heightInChanges(taskDto) + world.getHeightDeficit()));
        long initialHeightInChanges = heightInChanges(taskDto);
        log.info("new world height :" + (world.getHeightDeficit() + heightInChanges(taskDto)));
        long spendableHeightOnNewTiles = world.getHeightDeficit();

        continentalDriftNewTileAssigner.process(taskDto);
        log.info("post newTileAdjust height: "+ (worldHeight(world) + world.getHeightDeficit()));
        log.info("post newTileAdjust changes height: " + (heightInChanges(taskDto) + world.getHeightDeficit()));
        long heightInChanges = heightInChanges(taskDto);

        continentalDriftWorldAdjuster.process(taskDto);
        log.info("post driftWorldAdjuster height: "+ (worldHeight(world) + world.getHeightDeficit()));

        erosionAdjuster.process(taskDto);

        tileDefined.process(taskDto.getWorld());

        log.info("Post tileDefined height: "+ (worldHeight(world)+world.getHeightDeficit()));
        log.info("current tiles: " + world.getTiles().size());
        log.info("starting heightDeficit: " + initialHeight);
        log.info("current heightDeficit: " + world.getHeightDeficit());
        log.info("This turn " + spendableHeightOnNewTiles + " was spendable on new tiles, " + initialHeightInChanges + "in initial changes amd " + heightInChanges + "in heightchanges.");
        log.info("Proccesed a continentaldrift for world id: " + world.getId());

        return taskDto;
    }

    private Long heightInChanges(ContinentalDriftTaskDto taskDto) {
        return taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewTile()!=null).map(ContinentalChangesDto::getNewTile).map(Tile::getHeight).reduce(0L, (x, y) -> x+y);
    }

    private Long newMapHeight(ContinentalDriftTaskDto taskDto) {
        return taskDto.getNewTileLayout().values().stream().reduce(new ArrayList<>(), (x, y) -> {x.addAll(y); return x;}).stream().map(Tile::getHeight).reduce(0L, (x, y) -> x+y);
    }

    private Long worldHeight(World world) {
        return world.getTiles().stream().map(Tile::getHeight).reduce(0L, (x, y) -> x+y);
    }

    private ContinentalDriftTaskDto setup(World world) {
        ContinentalDriftTaskDto taskDto = new ContinentalDriftTaskDto();
        taskDto.setWorld(world);
        taskDto.setHeightDivider(heigtDivider);
        taskDto.setMinContinents(minimumContinents);

        return taskDto;
    }

}
