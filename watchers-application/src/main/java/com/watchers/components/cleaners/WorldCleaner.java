package com.watchers.components.cleaners;

import com.watchers.model.actors.Actor;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.StateType;
import com.watchers.model.environment.River;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.Watershed;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.ContinentRepository;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class WorldCleaner {

    private WorldRepository worldRepository;
    private ContinentRepository continentRepository;

    @Transactional
    public void proces(WorldTaskDto dto) {
        World world = worldRepository.findById(dto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        List<Actor> currentDeads = world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .collect(Collectors.toList());
        log.debug(currentDeads.size() + " Actors died");
        currentDeads.forEach(deadActor -> {
            if (deadActor.getCoordinate() != null) {
                deadActor.getCoordinate().getActors().remove(deadActor);
            }
            deadActor.setCoordinate(null);
        });

        log.debug(world.getActorList().size() + " Actors remained before cleansing the dead");

        world.getActorList().removeAll(currentDeads);

        log.debug(world.getNewActors().size() + " Actors were born into this world");
        world.getActorList().addAll(world.getNewActors());
        world.getNewActors().clear();

        log.debug(world.getActorList().size() + " Actors remaining");

        if (dto instanceof ContinentalDriftTaskDto) {
            List<Continent> zeroContinents = world.getContinents().stream().filter(continent -> continent.getCoordinates().size() == 0).collect(Collectors.toList());
            if (zeroContinents.size() > 0) {
                int start = world.getContinents().size();
                log.trace("deleting continents: " + Arrays.toString(zeroContinents.stream().map(Continent::getId).toArray()));
                zeroContinents.forEach(world.getContinents()::remove);

                worldRepository.save(world);

                world = worldRepository.findById(world.getId()).orElseThrow(() -> new RuntimeException("World was lost in memory"));
                continentRepository.deleteAll(zeroContinents);
                zeroContinents.stream().map(Continent::getId).forEach(aLong -> ((ContinentalDriftTaskDto) dto).getRemovedContinents.add(aLong));
                log.trace("Started with " + start + " continents and ended with " + world.getContinents().size());
            }

            Set<Watershed> watersheds = new HashSet<>(world.getWatersheds());

            world.getCoordinates().stream()
                    .map(Coordinate::getTile)
                    .filter(Tile::isSea)
                    .map(Tile::getRiver)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(river -> {
                        Tile tile = river.getTile();
                        if (tile != null) {
                            watersheds.forEach(watershed -> watershed.removeTile(tile));
                        }
                        river.getUpCurrentRivers().forEach(upcurrentRiver -> upcurrentRiver.setDownCurrentRiver(null));
                        river.getUpCurrentRivers().remove(river);
                        if (river.getDownCurrentRiver() != null
                                && river.getDownCurrentRiver().getUpCurrentRivers() != null) {
                            river.getDownCurrentRiver().getUpCurrentRivers().remove(river);
                        }
                        river.setDownCurrentRiver(null);
                        river.getTile().setRiver(null);
                        if (tile != null) {
                            river.getWatershed().removeTile(tile);
                        }
                    });

            world.getCoordinates().stream()
                    .map(Coordinate::getTile)
                    .filter(Tile::isSea)
                    .filter(tile -> tile.getWatershed() != null)
                    .forEach(tile -> tile.getWatershed().removeTile(tile));

            final World finalWorld = world;
            watersheds.stream()
                    .filter(watershed -> watershed.getWatershedTiles().isEmpty())
                    .forEach(finalWorld::removeWatershed);

        }

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getWatershed() == null)
                .forEach(tile -> tile.setRiver(null));

        worldRepository.save(world);
    }
}
