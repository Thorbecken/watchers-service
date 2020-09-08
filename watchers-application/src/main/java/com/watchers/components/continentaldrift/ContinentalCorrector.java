package com.watchers.components.continentaldrift;

import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.World;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContinentalCorrector {

    public void process(ContinentalDriftTaskDto driftTaskDto) {
        Assert.notNull(driftTaskDto.getWorld(), "World is not found!");
        World world = driftTaskDto.getWorld();

        world.getCoordinates().forEach(
                coordinate -> {
                    List<Coordinate> neighbours = coordinate.getNeighbours();
                    long count = neighbours.stream()
                            .filter(neighbouringCoordinate -> neighbouringCoordinate.getContinent().equals(coordinate.getContinent()))
                            .count();

                    if (count < 2) {
                        List<Long> continentalIdList = neighbours.stream().map(Coordinate::getContinent).map(Continent::getId).collect(Collectors.toList());
                        Long mostCommonContinent = mostCommon(continentalIdList);

                        Continent oldContinent = coordinate.getContinent();
                        oldContinent.getCoordinates().remove(coordinate);

                        Optional<Continent> chosenContinent = world.getContinents().stream()
                                .filter(continent -> continent.getId() != null)
                                .filter(continent -> continent.getId().equals(mostCommonContinent))
                                .findFirst();

                        chosenContinent.ifPresent( continent -> {
                            coordinate.setContinent(continent);
                            coordinate.getContinent().getCoordinates().add(coordinate);
                        });
                    }
                }
        );

    }

    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        assert max != null;
        return max.getKey();
    }

}
