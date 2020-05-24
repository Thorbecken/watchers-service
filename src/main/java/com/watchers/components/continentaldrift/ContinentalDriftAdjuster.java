package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ContinentalDriftAdjuster {

    private CoordinateHelper coordinateHelper;

    public ContinentalDriftAdjuster(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
    }

    public void calculateContinentalDrift(ContinentalDriftTaskDto taskDto){
        createButtomLayer(taskDto);
        taskDto.getWorld().getContinents().forEach(continent -> predictContinentalMovement(continent, taskDto.getNewTileLayout()));
    }

    private void predictContinentalMovement(Continent continent, Map<Coordinate, List<Tile>> buttomLayer){
        final int xVelocity = continent.getDirection().getXVelocity();
        final int yVelocity = continent.getDirection().getYVelocity();
        continent.getTiles()
                .forEach(tile -> {
                    Coordinate coordinate = tile.getCoordinate().calculateDistantCoordinate(xVelocity, yVelocity);
                    List<Tile> tiles = buttomLayer.get(coordinate);
                    tiles.add(tile);
                });
    }

    private void createButtomLayer(ContinentalDriftTaskDto taskDto) {
        Map<Coordinate,List<Tile>> map = taskDto.getNewTileLayout();
        coordinateHelper.getAllPossibleCoordinates(taskDto.getWorld())
                .forEach(coordinate -> map.put(coordinate, new ArrayList<>()));
    }
}
