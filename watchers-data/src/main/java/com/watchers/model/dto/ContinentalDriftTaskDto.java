package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.WorldMetaData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContinentalDriftTaskDto extends WorldTaskDto {

    private long heightLoss;

    private List<Tile> toBeRemovedTiles = new ArrayList<>();
    private Map<Coordinate, List<Tile>> newTileLayout = new HashMap<>();
    private Map<Coordinate, ContinentalChangesDto> changes = new HashMap<>();
    public List<Long> getRemovedContinents = new ArrayList<>();

    public ContinentalDriftTaskDto(WorldMetaData worldMetaData){
        this(worldMetaData.getId(), worldMetaData.isNeedsSaving(), worldMetaData.isNeedsContinentalShift());
    }

    public ContinentalDriftTaskDto(Long worldId, boolean needsSaving, boolean needsContinentaldrift) {
        super(worldId, needsSaving, needsContinentaldrift);
    }
}
