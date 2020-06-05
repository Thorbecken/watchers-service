package com.watchers.components.continentaldrift;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TileDefined {

    private long deepOceanHight;
    private long oceeanHight;
    private long coastalHight;
    private long hillHight;
    private long mountainHight;

    public TileDefined(@Value("${watch.deepOceanHight}") long deepOceanHight,
                       @Value("${watch.oceeanHight}") long oceeanHight,
                       @Value("${watch.coastalHight}") long coastalHight,
                       @Value("${watch.hillHight}") long hillHight,
                       @Value("${watch.mountainHight}") long mountainHight){
        this.deepOceanHight = deepOceanHight;
        this.oceeanHight = oceeanHight;
        this.coastalHight = coastalHight;
        this.hillHight = hillHight;
        this.mountainHight = mountainHight;
    }

    public void process(World world) {

        world.getTiles().parallelStream().forEach(tile -> {
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
}
