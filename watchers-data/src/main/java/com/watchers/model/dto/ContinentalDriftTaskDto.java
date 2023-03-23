package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.WorldMetaData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContinentalDriftTaskDto extends WorldTaskDto {

    private long heightLoss;

    private List<Tile> toBeRemovedTiles = new ArrayList<>();
    private Map<MockCoordinate, List<MockTile>> newTileLayout = new HashMap<>();
    private Map<MockCoordinate, ContinentalChangesDto> changes = new HashMap<>();
    public List<Long> getRemovedContinents = new ArrayList<>();

    public List<MockTile> getCoordinateChangeList(MockCoordinate coordinate) {
        return newTileLayout.get(coordinate);
    }

    public void addChange(MockCoordinate mockCoordinate, ContinentalChangesDto dto) {
        this.changes.put(mockCoordinate, dto);
    }

    public ContinentalChangesDto getChange(Coordinate coordinate) {
        return this.changes.get(new MockCoordinate(coordinate));
    }

    public ContinentalDriftTaskDto(WorldMetaData worldMetaData) {
        super(worldMetaData.getId(), worldMetaData.isNeedsSaving(), worldMetaData.isNeedsContinentalShift());
        createButtomLayer(worldMetaData);
    }

    private void createButtomLayer(WorldMetaData worldMetaData) {
        this.newTileLayout = new HashMap<>();
        for (long x = 1; x <= worldMetaData.getXSize(); x++) {
            for (long y = 1; y <= worldMetaData.getYSize(); y++) {
                this.newTileLayout.put(new MockCoordinate(x, y), new ArrayList<>());
            }
        }
    }

    public void clearContinentalData() {
        this.changes.clear();
        this.newTileLayout.clear();
        this.toBeRemovedTiles.clear();
        this.getGetRemovedContinents().clear();
    }
}
