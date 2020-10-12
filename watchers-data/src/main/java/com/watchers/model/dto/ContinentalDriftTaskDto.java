package com.watchers.model.dto;

import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Tile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContinentalDriftTaskDto extends WorldTaskDto {

    private long heightLoss;
    private long heightDivider;
    private int minContinents;
    private List<Tile> toBeRemovedTiles = new ArrayList<>();
    private Map<Coordinate, List<Tile>> newTileLayout = new HashMap<>();
    private Map<Coordinate, ContinentalChangesDto> changes = new HashMap<>();
    public List<Long> getRemovedContinents = new ArrayList<>();

    public ContinentalDriftTaskDto(Long worldId) {
        super(worldId);
    }
}
