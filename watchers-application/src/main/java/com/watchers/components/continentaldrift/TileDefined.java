package com.watchers.components.continentaldrift;

import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TileDefined {

    private long deepOceanHight;
    private long oceeanHight;
    private long coastalHight;
    private long plainsHight;
    private long hillHight;
    private long mountainHight;

    private WorldRepositoryInMemory worldRepositoryInMemory;

    public TileDefined(@Value("${watch.deepOceanHight}") long deepOceanHight,
                       @Value("${watch.oceeanHight}") long oceeanHight,
                       @Value("${watch.coastalHight}") long coastalHight,
                       @Value("${watch.plainsHight}") long plainsHight,
                       @Value("${watch.hillHight}") long hillHight,
                       @Value("${watch.mountainHight}") long mountainHight, WorldRepositoryInMemory worldRepositoryInMemory){
        this.deepOceanHight = deepOceanHight;
        this.oceeanHight = oceeanHight;
        this.coastalHight = coastalHight;
        this.plainsHight = plainsHight;
        this.hillHight = hillHight;
        this.mountainHight = mountainHight;
        this.worldRepositoryInMemory = worldRepositoryInMemory;
    }

    public void setStartingHeights(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(
                tile -> {
                    switch (tile.getSurfaceType()){
                        case MOUNTAIN: tile.setHeight(mountainHight);
                            break;
                        case HILL: tile.setHeight(hillHight);
                            break;
                        case PLAIN: tile.setHeight(plainsHight);
                            break;
                        case OCEANIC: tile.setHeight(oceeanHight);
                            break;
                        case COASTAL: tile.setHeight(coastalHight);
                            break;
                        case DEEP_OCEAN: tile.setHeight(deepOceanHight);
                            break;
                        default: throw new RuntimeException("Surface type has not been given");
                    }
                }
        );
    }

    public void assignStartingType(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height <= deepOceanHight){
                tile.setSurfaceType(SurfaceType.DEEP_OCEAN);
            } else if (height <= oceeanHight){
                tile.setSurfaceType(SurfaceType.OCEANIC);
            } else if (height <= coastalHight){
                tile.setSurfaceType(SurfaceType.COASTAL);
            } else if (height <= hillHight){
                tile.setSurfaceType(SurfaceType.PLAIN);
            } else if (height <= mountainHight){
                tile.setSurfaceType(SurfaceType.HILL);
            } else {
                tile.setSurfaceType(SurfaceType.MOUNTAIN);
            }
        });
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(WorldTaskDto worldTaskDto) {
        World world = worldRepositoryInMemory.findById(worldTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));

        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height <= deepOceanHight){
                tile.setSurfaceType(SurfaceType.DEEP_OCEAN);
            } else if (height <= oceeanHight){
                tile.setSurfaceType(SurfaceType.OCEANIC);
            } else if (height <= coastalHight){
                tile.setSurfaceType(SurfaceType.COASTAL);
            } else if (height <= hillHight){
                tile.setSurfaceType(SurfaceType.PLAIN);
            } else if (height <= mountainHight){
                tile.setSurfaceType(SurfaceType.HILL);
            } else {
                tile.setSurfaceType(SurfaceType.MOUNTAIN);
            }
        });

        worldRepositoryInMemory.save(world);
    }
}
