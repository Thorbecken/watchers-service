package com.watchers.controller;

import com.watchers.manager.MapManager;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Slf4j
@Controller
@CrossOrigin
@SuppressWarnings("unused")
public class ActorController{

        private MapManager mapManager;
        private WorldRepositoryInMemory worldRepository;

    public ActorController(MapManager mapManager, WorldRepositoryInMemory worldRepository) {
        this.mapManager = mapManager;
        this.worldRepository = worldRepository;
    }

    @SuppressWarnings("unused")
    @RequestMapping(value = "/actors/{worldId}/{xCoord}/{yCoord}", method = RequestMethod.GET)
        public ResponseEntity seedLife(@PathVariable("worldId") Long worldId, @PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord){
            log.info("Received request to seed life at coordintae: " + xCoord + "x, " + yCoord + "y");
            Assert.notNull(worldId, "No world id was found");
            Assert.notNull(xCoord, "No xCoord was found");
            Assert.notNull(yCoord, "No yCoord was found");

            Optional<World> world = worldRepository.findById(worldId);
            if (world.isPresent()) {
                mapManager.seedLife(world.get(), xCoord, yCoord);
                worldRepository.save(world.get());
                log.info("Seeded life on world " + worldId + " at coordinates: " + xCoord + "x, " + yCoord + "y");
                return ResponseEntity.ok().build();
            } else {
                log.info("World not found");
            }

            return ResponseEntity.badRequest().build();
        }
}
