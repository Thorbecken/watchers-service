package com.watchers.controller;

import com.watchers.manager.MapManager;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Slf4j
@Controller
@CrossOrigin
public class ActorController{

        @Autowired
        private MapManager mapManager;

        @Autowired
        private WorldRepository worldRepository;

        @RequestMapping(value = "/actors/{worldId}/{xCoord}/{yCoord}", method = RequestMethod.GET)
        public ResponseEntity getWorldMap(@PathVariable("worldId") Long worldId, @PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord){
            log.info("Received request to seed life at coordintae: " + xCoord + "x, " + yCoord + "y");
            if(worldId != null) {
                Optional<World> world = worldRepository.findById(worldId);
                if (world.isPresent() && xCoord != null && yCoord != null) {
                    mapManager.seedLife(world.get(), xCoord, yCoord);
                    worldRepository.save(world.get());
                    log.info("Seeded life on world " + worldId + " at coordinates: " + xCoord + "x, " + yCoord + "y");
                    return ResponseEntity.ok().build();
                } else {
                    log.info("World not found");
                }
            }

            return ResponseEntity.badRequest().build();
        }
}
