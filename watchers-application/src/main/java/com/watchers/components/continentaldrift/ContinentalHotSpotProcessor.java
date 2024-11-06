package com.watchers.components.continentaldrift;

import com.watchers.helper.RandomHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalHotSpotProcessor {

    private static final long MINIMUM_HEIGHT_BUILDUP_FOR_ERUPTION = 60;
    private static final long NUMBER_OF_TURNS_BEFORE_REALLOCATION = 178L;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        int numberOfMantlePlumes = world.getWorldSettings().getNumberOfMantlePlumes();
        List<HotSpotCrystal> hotSpotCrystals = world.getCoordinates().stream()
                .map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof HotSpotCrystal)
                .map(pointOfInterest -> ((HotSpotCrystal) pointOfInterest))
                .collect(Collectors.toList());
        while (numberOfMantlePlumes > (hotSpotCrystals.size())) {
            long x = RandomHelper.getRandomNonZero(world.getXSize());
            long y = RandomHelper.getRandomNonZero(world.getYSize());
            Coordinate coordinate = world.getCoordinate(x, y);
            hotSpotCrystals.add(new HotSpotCrystal(coordinate));
            log.info("created hotSpotCrystal with coordinate " + coordinate.toString());
            log.info(coordinate.getPointOfInterest().getDescription());
        }

        while (world.getHeightDeficit() > 0) {
            log.trace("Current height deficit: " + world.getHeightDeficit() + " meter(s).");
            for (HotSpotCrystal hotSpotCrystal : hotSpotCrystals) {
                long heightdefecit = world.getHeightDeficit();
                if(heightdefecit > 0) {
                    long extraHeight = RandomHelper.getRandomLong(world.getHeightDeficit());
                    log.trace("HotSpotCrystal gained " + extraHeight + " meter(s) height buildup.");
                    hotSpotCrystal.addHeightBuildup(extraHeight);
                    world.setHeightDeficit(world.getHeightDeficit() - extraHeight);
                }
            }
        }

        for (HotSpotCrystal hotSpotCrystal : hotSpotCrystals) {
            long heightBuildup = hotSpotCrystal.getHeightBuildup();
            if (heightBuildup > MINIMUM_HEIGHT_BUILDUP_FOR_ERUPTION) {
                Tile tile = hotSpotCrystal.getCoordinate().getTile();
                tile.setHeight(tile.getHeight() + heightBuildup);
                hotSpotCrystal.setHeightBuildup(0);
            }

            hotSpotCrystal.setTimer(hotSpotCrystal.getTimer() - 1);
            if (hotSpotCrystal.getTimer() <= 0) {
                long x = RandomHelper.getRandomNonZero(world.getXSize());
                long y = RandomHelper.getRandomNonZero(world.getYSize());
                Coordinate coordinate = world.getCoordinate(x, y);
                hotSpotCrystal.setCoordinate(coordinate);
                hotSpotCrystal.setTimer(NUMBER_OF_TURNS_BEFORE_REALLOCATION);
            }
        }
    }
}
