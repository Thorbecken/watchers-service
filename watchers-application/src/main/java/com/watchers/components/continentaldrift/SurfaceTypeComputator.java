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

    private final long seaHeight;
    private final long coastalHeight;
    private final long plainsHeight;
    private final long hillHeight;
    private final long mountainHeight;

    public SurfaceTypeComputator(
                       @Value("${watch.seaHeight}") long seaHeight,
                       @Value("${watch.coastalHeight}") long coastalHeight,
                       @Value("${watch.plainsHeight}") long plainsHeight,
                       @Value("${watch.hillHeight}") long hillHeight,
                       @Value("${watch.mountainHeight}") long mountainHeight){
        this.seaHeight = seaHeight;
        this.coastalHeight = coastalHeight;
        this.plainsHeight = plainsHeight;
        this.hillHeight = hillHeight;
        this.mountainHeight = mountainHeight;
    }

    @Transactional
    public void process(WorldTaskDto worldTaskDto) {
        World world = worldTaskDto.getWorld();

        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height >= mountainHeight){
                tile.setSurfaceType(SurfaceType.MOUNTAIN);
            } else if (height >= hillHeight){
                tile.setSurfaceType(SurfaceType.HILL);
            } else if (height >= plainsHeight){
                tile.setSurfaceType(SurfaceType.PLAIN);
            } else if (height >= coastalHeight){
                tile.setSurfaceType(SurfaceType.COASTAL);
            } else if (height >= seaHeight){
                tile.setSurfaceType(SurfaceType.SEA);
            } else {
                tile.setSurfaceType(SurfaceType.OCEAN);
            }
        });
    }
}
