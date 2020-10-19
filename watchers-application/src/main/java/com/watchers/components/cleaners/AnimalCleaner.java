package com.watchers.components.cleaners;

import com.watchers.model.actor.Actor;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AnimalCleaner {

    private WorldRepositoryInMemory worldRepositoryInMemory;

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(ContinentalDriftTaskDto continentalDriftTaskDto) {
        World world = worldRepositoryInMemory.findById(continentalDriftTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memoroy"));

        world.getActorList().stream()
                .filter(Actor::isNotOnCorrectLand)
                .forEach(Actor::handleContinentalMovement);

        worldRepositoryInMemory.save(world);
    }
}
