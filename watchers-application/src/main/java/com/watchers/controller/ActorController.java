package com.watchers.controller;

import com.watchers.manager.LifeManager;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(description = "This controller exposes functionalitie about all the actors in the worlds.")
@SuppressWarnings("unused")
public class ActorController{

        private LifeManager lifeManager;
        private WorldRepositoryInMemory worldRepository;

    public ActorController(LifeManager lifeManager, WorldRepositoryInMemory worldRepository) {
        this.worldRepository = worldRepository;
        this.lifeManager = lifeManager;
    }

    @SuppressWarnings("unused")
    @ApiOperation(value = "Generates a new actor on the chosen coordinate.")
    @RequestMapping(value = "/actors/{worldId}/{xCoord}/{yCoord}", method = RequestMethod.GET)
    //TODO maak hier nog een post method van. of put.
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
