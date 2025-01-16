package com.watchers.components.continentaldrift;

import com.watchers.components.ContinentalComputer;
import com.watchers.helper.RandomHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.HotSpotCrystal;
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
public class ContinentalHotSpotComputer implements ContinentalComputer {

    public ContinentalHotSpotComputer(
            @Value("${watch.continent.volcano.buildup.minimum}") long minimumHeightBuildupForEruption,
            @Value("${watch.continent.volcano.height.maximum}") int maximumHeight,
            @Value("${watch.continent.volcano.turn-limit}") int numberOfTurnsBeforeReallocation,
            @Value("${watch.continent.volcano.number.minimum}") int minimumNumberOfVolcano){
        this.minimumHeightBuildupForEruption = minimumHeightBuildupForEruption;
        this.numberOfTurnsBeforeReallocation = numberOfTurnsBeforeReallocation;
        this.minimumNumberOfVolcano = minimumNumberOfVolcano;
        this.maximumHeight = maximumHeight;
    }

    private final long minimumHeightBuildupForEruption;
    private final int numberOfTurnsBeforeReallocation;
    private final int minimumNumberOfVolcano;
    private final int maximumHeight;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        List<HotSpotCrystal> hotSpotCrystals = world.getCoordinates().stream()
                .map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof HotSpotCrystal)
                .map(pointOfInterest -> ((HotSpotCrystal) pointOfInterest))
                .collect(Collectors.toList());
        while (hotSpotCrystals.size() < minimumNumberOfVolcano) {
            long x = RandomHelper.getRandomNonZero(world.getXSize());
            long y = RandomHelper.getRandomNonZero(world.getYSize());
            Coordinate coordinate = world.getCoordinate(x, y);
            hotSpotCrystals.add(new HotSpotCrystal(coordinate, numberOfTurnsBeforeReallocation));
            log.info("created hotSpotCrystal with coordinate " + coordinate.toString());
            log.info(coordinate.getPointOfInterest().getDescription());
        }

        while (world.getHeightDeficit() > 0) {
            log.trace("Current height deficit: " + world.getHeightDeficit() + " meter(s).");
            hotSpotCrystals.sort(Comparator.comparing(HotSpotCrystal::getHeightBuildup));
            for (HotSpotCrystal hotSpotCrystal : hotSpotCrystals) {
                long heightDeficit = world.getHeightDeficit();
                if (heightDeficit > 0) {
                    long extraHeight = RandomHelper.getRandomLong(world.getHeightDeficit());
                    log.trace("HotSpotCrystal gained " + extraHeight + " meter(s) height buildup.");
                    hotSpotCrystal.addHeightBuildup(extraHeight);
                    world.setHeightDeficit(world.getHeightDeficit() - extraHeight);
                }
            }
        }

        for (HotSpotCrystal hotSpotCrystal : hotSpotCrystals) {
            long heightBuildup = hotSpotCrystal.getHeightBuildup();
            if (heightBuildup >= minimumHeightBuildupForEruption) {
                Tile tile = hotSpotCrystal.getCoordinate().getTile();
                if(tile.getHeight() < maximumHeight) {
                    tile.setHeight(tile.getHeight() + heightBuildup);
                    hotSpotCrystal.setHeightBuildup(0);
                }
            }

            hotSpotCrystal.setTimer(hotSpotCrystal.getTimer() - 1);
            if (hotSpotCrystal.getTimer() <= 0) {
                long x = RandomHelper.getRandomNonZero(world.getXSize());
                long y = RandomHelper.getRandomNonZero(world.getYSize());
                Coordinate coordinate = world.getCoordinate(x, y);
                hotSpotCrystal.setCoordinate(coordinate);
                hotSpotCrystal.setTimer(numberOfTurnsBeforeReallocation);
            }
        }
    }
}
