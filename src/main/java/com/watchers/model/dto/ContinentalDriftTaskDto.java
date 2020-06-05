package com.watchers.model.dto;

import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ContinentalDriftTaskDto {

    private World world;
    private long heightLoss;
    private long heightDivider;
    private int minContinents;
    private Map<Coordinate, List<Tile>> newTileLayout;
    private Map<Coordinate, ContinentalChangesDto> changes;

    public ContinentalDriftTaskDto(){
        this.changes = new HashMap<>();
        this.newTileLayout = new HashMap<>();
    }

}
