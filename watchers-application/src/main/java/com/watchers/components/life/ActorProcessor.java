package com.watchers.components.life;

import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ActorProcessor {

    private WorldRepositoryInMemory worldRepositoryInMemory;

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(WorldTaskDto taskDto){
        World world = worldRepositoryInMemory.findById(taskDto.getWorldId()).orElseThrow(()-> new RuntimeException("The world was lost in memory."));

        log.debug(world.getActorList().size() + " Actors at the start of this turn");
        log.debug(world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .count() + " Actors where dead at the start of this turn");

        List<Actor> currentActors = world.getActorList();
        Assert.notNull(currentActors, "There was no list found of Actors!");
        if (currentActors.size() == 0){
            log.warn("The actorslist was empty!");
        } else {
            currentActors.forEach(Actor::processSerialTask);
        }

        worldRepositoryInMemory.save(world);
    }
}
