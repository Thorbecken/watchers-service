package com.watchers.components.continentaldrift;

import com.watchers.components.ContinentalComputer;
import com.watchers.helper.CoordinateHelper;
import com.watchers.helper.RandomHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.model.world.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ContinentalMantelPlumeComputer implements ContinentalComputer {

    private final int numberOfTurnsBeforeReallocation;
    private final int minimumNumberOfPlume;

    public ContinentalMantelPlumeComputer(
            @Value("${watch.continent.mantle-plume.turn-limit}") int numberOfTurnsBeforeReallocation,
            @Value("${watch.continent.mantle-plume.amount.minimum}") int minimumNumberOfPlume){
        this.numberOfTurnsBeforeReallocation = numberOfTurnsBeforeReallocation;
        this.minimumNumberOfPlume = minimumNumberOfPlume;
    }

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        List<TectonicCrystal> tectonicCrystals = world.getCoordinates().stream()
                .map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof TectonicCrystal)
                .map(pointOfInterest -> ((TectonicCrystal) pointOfInterest))
                .collect(Collectors.toList());
        while (tectonicCrystals.size() < minimumNumberOfPlume) {
            long x = RandomHelper.getRandomNonZero(world.getXSize());
            long y = RandomHelper.getRandomNonZero(world.getYSize());
            Coordinate coordinate = world.getCoordinate(x, y);
            tectonicCrystals.add(new TectonicCrystal(coordinate, numberOfTurnsBeforeReallocation));
            log.info("created tectonicecrystal with coordinate " + coordinate.toString());
            log.info(coordinate.getPointOfInterest().getDescription());
        }
        for (TectonicCrystal tectonicCrystal : tectonicCrystals) {
            tectonicCrystal.setTimer(tectonicCrystal.getTimer() - 1);
            if (tectonicCrystal.getTimer() <= 0) {
                long x = RandomHelper.getRandomNonZero(world.getXSize());
                long y = RandomHelper.getRandomNonZero(world.getYSize());
                Coordinate coordinate = world.getCoordinate(x, y);
                tectonicCrystal.setCoordinate(coordinate);
                tectonicCrystal.setTimer(numberOfTurnsBeforeReallocation);
            }
        }

        world.getContinents().stream()
                .filter(continent -> continent.getCoordinates().size() > 0)
                .forEach(continent -> {
                    Coordinate meanCoordinate = CoordinateHelper.getMeanCoordinate(continent);
                    tectonicCrystals.stream()
                            .min(Comparator.comparing(tectonicCrystal -> tectonicCrystal.getCoordinate().getDistance(meanCoordinate)))
                            .ifPresent(tectonicCrystal -> continent.getDirection().addPressure(tectonicCrystal, meanCoordinate, world.getWorldSettings().getDriftVelocity()));
                });
    }
}
