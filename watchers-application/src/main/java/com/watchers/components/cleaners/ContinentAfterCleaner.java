package com.watchers.components.cleaners;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class ContinentAfterCleaner {

    @Transactional
    public void process(ContinentalDriftTaskDto dto) {
        if (dto.getGetRemovedContinents().size() > 0) {
            World world = dto.getWorld();

            Long lastId = world.getLastContinentInFlux();

            if (dto.getRemovedContinents.stream().anyMatch(id -> id.equals(lastId))) {
                Long newLastContinentInFlux = world.getContinents().stream().map(Continent::getId).max(Long::compareTo).orElse(1L);
                log.info("Continent " + lastId + " was deleted. The new lastContinentInFlux is: " + newLastContinentInFlux);
                log.warn("setting last continent in flux to " + newLastContinentInFlux + " from continent cleaner");
                world.setLastContinentInFlux(newLastContinentInFlux);
            }
        }
    }
}
