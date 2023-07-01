package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalIntegretyAdjuster {

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Set<Continent> continents = new HashSet<>(world.getContinents());
        continents.forEach(this::checkContinent);
    }

    private void checkContinent(Continent continent) {
        List<Set<Coordinate>> checkedCoordinatesList = CoordinateHelper.getListsOfAdjacentCoordinatesFromContinent(continent);
        if (checkedCoordinatesList.size() > 1) {
            log.info("Continent " + continent.getId() + " was split into " + checkedCoordinatesList.size() + " parts");
            World world = continent.getWorld();
            for (int i = 0; i < checkedCoordinatesList.size(); i++) {
                if (i != 0) {
                    createNewContinent(checkedCoordinatesList.get(i), world, continent.getType());
                }
            }
        }

    }

    private void createNewContinent(Collection<Coordinate> coordinates, World world, SurfaceType type) {
        Continent newContinent = new Continent(world, type);
        coordinates.forEach(coordinate -> coordinate.changeContinent(newContinent));
    }
}
