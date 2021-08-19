package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TileDefined {

    private final long oceanHight;
    private final long seaHight;
    private final long coastalHight;
    private final long plainsHight;
    private final long hillHight;
    private final long mountainHight;

    public TileDefined(@Value("${watch.oceanHight}") long oceanHight,
                       @Value("${watch.seaHight}") long seaHight,
                       @Value("${watch.coastalHight}") long coastalHight,
                       @Value("${watch.plainsHight}") long plainsHight,
                       @Value("${watch.hillHight}") long hillHight,
                       @Value("${watch.mountainHight}") long mountainHight){
        this.oceanHight = oceanHight;
        this.seaHight = seaHight;
        this.coastalHight = coastalHight;
        this.plainsHight = plainsHight;
        this.hillHight = hillHight;
        this.mountainHight = mountainHight;
    }

    public void setStartingHeights(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(
                tile -> {
                    switch (tile.getSurfaceType()){
                        case OCEAN: tile.setHeight(oceanHight);
                            break;
                        case SEA: tile.setHeight(seaHight);
                            break;
                        case COASTAL: tile.setHeight(coastalHight);
                            break;
                        case PLAIN: tile.setHeight(plainsHight);
                            break;
                        case HILL: tile.setHeight(hillHight);
                            break;
                        case MOUNTAIN: tile.setHeight(mountainHight);
                            break;
                        default: throw new RuntimeException("Surface type has not been given");
                    }
                }
        );
    }

    public void assignStartingType(World world){
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(tile -> {
            long height = tile.getHeight();
            if(height <= oceanHight){
                tile.setSurfaceType(SurfaceType.OCEAN);
            } else if (height <= seaHight){
                tile.setSurfaceType(SurfaceType.SEA);
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
}
