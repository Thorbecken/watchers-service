package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockCoordinate;
import com.watchers.model.dto.MockTile;
import com.watchers.model.world.Continent;
import com.watchers.repository.ContinentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class ContinentalDriftPredicter {

    private ContinentRepository continentRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        List<Continent> continents = continentRepository.findAll();
        continents.forEach(continent -> predictContinentalMovement(continent, taskDto));
    }

    @Transactional
    private void predictContinentalMovement(Continent continent, ContinentalDriftTaskDto taskDto) {
        final int xVelocity = continent.getDirection().getXVelocity();
        final int yVelocity = continent.getDirection().getYVelocity();
        continent.getCoordinates()
                .forEach(coordinate -> {
                    Coordinate predictedCoordinate = coordinate.calculateDistantCoordinate(xVelocity, yVelocity);
                    List<MockTile> mockTiles = taskDto.getCoordinateChangeList(new MockCoordinate(predictedCoordinate));
                    mockTiles.add(new MockTile(coordinate.getTile()));
                });
    }
}
