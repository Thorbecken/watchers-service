package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SurfaceTypeComputator {

    private final long seaHight;
    private final long coastalHight;
    private final long plainsHight;
    private final long hillHight;
    private final long mountainHight;

    public SurfaceTypeComputator(
                       @Value("${watch.seaHight}") long seaHight,
                       @Value("${watch.coastalHight}") long coastalHight,
                       @Value("${watch.plainsHight}") long plainsHight,
                       @Value("${watch.hillHight}") long hillHight,
                       @Value("${watch.mountainHight}") long mountainHight){
        this.seaHight = seaHight;
        this.coastalHight = coastalHight;
        this.plainsHight = plainsHight;
        this.hillHight = hillHight;
        this.mountainHight = mountainHight;
    }

    @Transactional
    public void process(WorldTaskDto worldTaskDto) {
        World world = worldTaskDto.getWorld();

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
    }
}
