package com.watchers.model.dto;

import com.watchers.model.WorldSettings;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Tile;
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

    private long heightDivider;
    private int minContinents;

    public ContinentalDriftTaskDto(WorldSettings worldSettings){
        this(worldSettings.getWorldId(), worldSettings.isNeedsSaving(), worldSettings.isNeedsContinentalShift(), worldSettings.getHeigtDivider(), worldSettings.getMinimumContinents());
    }

    public ContinentalDriftTaskDto(Long worldId, boolean needsSaving, boolean needsContinentaldrift, long heigtDivider, int minimumContinents) {
        super(worldId, needsSaving, needsContinentaldrift);
        this.heightDivider = heigtDivider;
        this.minContinents = minimumContinents;
    }
}
