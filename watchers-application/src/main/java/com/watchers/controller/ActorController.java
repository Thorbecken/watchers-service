package com.watchers.controller;

import com.watchers.manager.LifeManager;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@SuppressWarnings("unused")
public class ActorController {

    private final LifeManager lifeManager;
    private final WorldRepository worldRepository;

    @RequestMapping(value = "/fauna/{xCoord}/{yCoord}", method = RequestMethod.PUT)
    public ResponseEntity seedLife(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to seed fauna at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        lifeManager.seedLife(xCoord, yCoord);
        log.info("Seeded life at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/flora/{xCoord}/{yCoord}", method = RequestMethod.PUT)
    public ResponseEntity seedPlant(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to seed f at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        lifeManager.seedFlora(xCoord, yCoord);
        log.info("Seeded plant at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }
}
