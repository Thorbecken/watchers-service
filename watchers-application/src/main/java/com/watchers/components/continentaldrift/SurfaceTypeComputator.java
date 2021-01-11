package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SurfaceTypeComputator {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private long seaHight;
    private long coastalHight;
    private long plainsHight;
    private long hillHight;
    private long mountainHight;

    public SurfaceTypeComputator(
                       @Value("${watch.seaHight}") long seaHight,
                       @Value("${watch.coastalHight}") long coastalHight,
                       @Value("${watch.plainsHight}") long plainsHight,
                       @Value("${watch.hillHight}") long hillHight,
                       @Value("${watch.mountainHight}") long mountainHight,
                       WorldRepositoryInMemory worldRepositoryInMemory){
        this.seaHight = seaHight;
        this.coastalHight = coastalHight;
        this.plainsHight = plainsHight;
        this.hillHight = hillHight;
        this.mountainHight = mountainHight;
        this.worldRepositoryInMemory = worldRepositoryInMemory;
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(WorldTaskDto worldTaskDto) {
        World world = worldRepositoryInMemory.findById(worldTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));

        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height >= mountainHight){
                tile.setSurfaceType(SurfaceType.MOUNTAIN);
            } else if (height >= hillHight){
                tile.setSurfaceType(SurfaceType.HILL);
            } else if (height >= plainsHight){
                tile.setSurfaceType(SurfaceType.PLAIN);
            } else if (height >= coastalHight){
                tile.setSurfaceType(SurfaceType.COASTAL);
            } else if (height >= seaHight){
                tile.setSurfaceType(SurfaceType.SEA);
            } else {
                tile.setSurfaceType(SurfaceType.OCEAN);
            }
        });

        worldRepositoryInMemory.save(world);
    }
}
