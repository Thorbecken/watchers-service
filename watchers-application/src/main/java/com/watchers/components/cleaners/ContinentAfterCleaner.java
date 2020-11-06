package com.watchers.components.cleaners;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class ContinentAfterCleaner {

    private final WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto dto) {
        if (dto.getGetRemovedContinents().size() > 0) {
            World world = worldRepository.findById(dto.getWorldId()).orElseThrow(() -> new RuntimeException("World was lost in memory"));

            Long lastId = world.getLastContinentInFlux();

            if (dto.getRemovedContinents.stream().anyMatch(id -> id.equals(lastId))) {
                Long newLastContinentInFlux = world.getContinents().stream().map(Continent::getId).max(Long::compareTo).orElse(1L);
                log.info("Continent " + lastId + " was deleted. The new lastContinentInFlux is: " + newLastContinentInFlux);
                world.setLastContinentInFlux(newLastContinentInFlux);
            }

            worldRepository.save(world);
        }
    }
}
