package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ContinentalCorrector {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto driftTaskDto) {
        World world = worldRepository.findById(driftTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Assert.notNull(world, "World is not found!");

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
                            coordinate.changeContinent(continent);
                            coordinate.getContinent().getCoordinates().add(coordinate);
                        });
                    }
                }
        );

        worldRepository.save(world);
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
