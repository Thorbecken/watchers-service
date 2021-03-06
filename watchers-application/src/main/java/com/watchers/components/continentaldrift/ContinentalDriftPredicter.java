package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class ContinentalDriftPredicter {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto){
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        createButtomLayer(taskDto, world);
        world.getContinents().forEach(continent -> predictContinentalMovement(continent, taskDto.getNewTileLayout()));
        worldRepository.save(world);
    }

    private void predictContinentalMovement(Continent continent, Map<Coordinate, List<Tile>> newTileLayout){
        final int xVelocity = continent.getDirection().getXVelocity();
        final int yVelocity = continent.getDirection().getYVelocity();
        continent.getCoordinates()
                .forEach(coordinate -> {
                    Coordinate predictedCoordinate = coordinate.calculateDistantCoordinate(xVelocity, yVelocity);
                    List<Tile> tiles = newTileLayout.get(predictedCoordinate);
                    tiles.add(coordinate.getTile());
                });
    }

    private void createButtomLayer(ContinentalDriftTaskDto taskDto, World world) {
        Map<Coordinate,List<Tile>> map = taskDto.getNewTileLayout();
        CoordinateHelper.getAllPossibleCoordinates(world)
                .forEach(coordinate -> map.put(coordinate, new ArrayList<>()));
    }
}
