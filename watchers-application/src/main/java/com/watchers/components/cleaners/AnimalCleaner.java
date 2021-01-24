package com.watchers.components.cleaners;

import com.watchers.model.actors.Actor;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AnimalCleaner {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto continentalDriftTaskDto) {
        World world = worldRepository.findById(continentalDriftTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memoroy"));

        world.getActorList().stream()
                .filter(Actor::isNotOnCorrectLand)
                .forEach(Actor::handleContinentalMovement);

        worldRepository.save(world);
    }
}
