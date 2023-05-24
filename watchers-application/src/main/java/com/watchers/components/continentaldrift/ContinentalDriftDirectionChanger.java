package com.watchers.components.continentaldrift;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalDriftDirectionChanger {

    public void assignFirstOrNewDriftDirections(World world) {
        world.getContinents().forEach(continent -> continent.assignNewDriftDirection(world.getWorldSettings().getDriftVelocity(), world));
    }

    public void assignFirstDriftDirrecion(Continent continent, World world) {
        continent.assignNewDriftDirection(world.getWorldSettings().getDriftVelocity(), world);
    }

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        adjustForDriftPressure(world);
    }

    private void adjustForDriftPressure(World world) {
        world.getContinents().stream()
                .map(Continent::getDirection)
                .forEach(direction -> {
                    direction.setVelocityFromPressure(world.getWorldSettings().getDriftVelocity());
                    direction.resetPressures();
                });
    }
}
