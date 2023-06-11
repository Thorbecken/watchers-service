package com.watchers.components.continentaldrift;

import com.watchers.helper.RandomHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalMantelPlumeProcessor {

    @Transactional
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        int numberOfMantlePlumes = world.getWorldSettings().getNumberOfMantlePlumes();
        List<TectonicCrystal> tectonicCrystals = world.getCoordinates().stream()
                .map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof TectonicCrystal)
                .map(pointOfInterest -> ((TectonicCrystal) pointOfInterest))
                .collect(Collectors.toList());
        while (numberOfMantlePlumes > (tectonicCrystals.size())) {
            long x = RandomHelper.getRandomNonZero(world.getXSize());
            long y = RandomHelper.getRandomNonZero(world.getYSize());
            Coordinate coordinate = world.getCoordinate(x, y);
            tectonicCrystals.add(new TectonicCrystal(coordinate));
            log.info("created tectonicecrystal with coordinate " + coordinate.toString());
            log.info(coordinate.getPointOfInterest().getDescription());
        }
        for (TectonicCrystal tectonicCrystal: tectonicCrystals) {
            tectonicCrystal.setTimer(tectonicCrystal.getTimer() - 1);
            if(tectonicCrystal.getTimer() <= 0){
                long x = RandomHelper.getRandomNonZero(world.getXSize());
                long y = RandomHelper.getRandomNonZero(world.getYSize());
                Coordinate coordinate = world.getCoordinate(x, y);
                tectonicCrystal.setCoordinate(coordinate);
                tectonicCrystal.setTimer(178L);
            }
        }

        world.getContinents().stream()
                .filter(continent -> continent.getCoordinates().size() > 0)
                .forEach(continent -> {
                    double meanX = continent.getCoordinates()
                            .stream().mapToLong(Coordinate::getXCoord)
                            .average()
                            .getAsDouble();
                    double meanY = continent.getCoordinates()
                            .stream().mapToLong(Coordinate::getYCoord)
                            .average()
                            .getAsDouble();

                    Coordinate meanCoordinate = world.getCoordinate(((long) meanX), ((long) meanY));
                    tectonicCrystals.stream()
                            .min(Comparator.comparing(tectonicCrystal -> tectonicCrystal.getCoordinate().getDistance(meanCoordinate)))
                            .ifPresent(tectonicCrystal -> continent.getDirection().addPressure(tectonicCrystal, meanCoordinate, world.getWorldSettings().getDriftVelocity()));
                });
    }
}
