package com.watchers.controller;

import com.watchers.manager.LifeManager;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Controller
@CrossOrigin
@SuppressWarnings("unused")
public class ActorController{

        private LifeManager lifeManager;
        private WorldRepository worldRepository;

    public ActorController(LifeManager lifeManager, WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
        this.lifeManager = lifeManager;
    }

    @SuppressWarnings("unused")
    @RequestMapping(value = "/actors/{worldId}/{xCoord}/{yCoord}", method = RequestMethod.GET)
        public ResponseEntity seedLife(@PathVariable("worldId") Long worldId, @PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord){
            log.info("Received request to seed life at coordintae: " + xCoord + "x, " + yCoord + "y");
            Assert.notNull(worldId, "No world id was found");
            Assert.notNull(xCoord, "No xCoord was found");
            Assert.notNull(yCoord, "No yCoord was found");

            if (worldRepository.existsById(worldId)) {
                lifeManager.seedLife(worldId, xCoord, yCoord);
                log.info("Seeded life on world " + worldId + " at coordinates: " + xCoord + "x, " + yCoord + "y");
                return ResponseEntity.ok().build();
            } else {
                log.info("World not found");
            }

            return ResponseEntity.badRequest().build();
        }
}
