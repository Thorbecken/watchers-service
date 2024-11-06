package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TileDefined {

    private final long oceanHeight;
    private final long seaHeight;
    private final long coastalHeight;
    private final long plainsHeight;
    private final long hillHeight;
    private final long mountainHeight;

    public TileDefined(@Value("${watch.oceanHeight}") long oceanHeight,
                       @Value("${watch.seaHeight}") long seaHeight,
                       @Value("${watch.coastalHeight}") long coastalHeight,
                       @Value("${watch.plainsHeight}") long plainsHeight,
                       @Value("${watch.hillHeight}") long hillHeight,
                       @Value("${watch.mountainHeight}") long mountainHeight){
        this.oceanHeight = oceanHeight;
        this.seaHeight = seaHeight;
        this.coastalHeight = coastalHeight;
        this.plainsHeight = plainsHeight;
        this.hillHeight = hillHeight;
        this.mountainHeight = mountainHeight;
    }

    public void setStartingHeights(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(
                tile -> {
                    switch (tile.getSurfaceType()){
                        case OCEAN: tile.setHeight(oceanHeight);
                            break;
                        case SEA: tile.setHeight(seaHeight);
                            break;
                        case COASTAL: tile.setHeight(coastalHeight);
                            break;
                        case PLAIN: tile.setHeight(plainsHeight);
                            break;
                        case HILL: tile.setHeight(hillHeight);
                            break;
                        case MOUNTAIN: tile.setHeight(mountainHeight);
                            break;
                        default: throw new RuntimeException("Surface type has not been given");
                    }
                }
        );
    }

    public void assignStartingType(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height <= oceanHeight){
                tile.setSurfaceType(SurfaceType.OCEAN);
            } else if (height <= seaHeight){
                tile.setSurfaceType(SurfaceType.SEA);
            } else if (height <= coastalHeight){
                tile.setSurfaceType(SurfaceType.COASTAL);
            } else if (height <= hillHeight){
                tile.setSurfaceType(SurfaceType.PLAIN);
            } else if (height <= mountainHeight){
                tile.setSurfaceType(SurfaceType.HILL);
            } else {
                tile.setSurfaceType(SurfaceType.MOUNTAIN);
            }
        });
    }
}
