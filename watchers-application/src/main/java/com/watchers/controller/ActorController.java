package com.watchers.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.manager.LifeManager;
import com.watchers.model.common.Views;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.environment.Flora;
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

    @JsonView(Views.Public.class)
    @RequestMapping(value = "/fauna", method = RequestMethod.GET)
    public ResponseEntity getFauna() {
        log.info("Received request about fauna");
        return ResponseEntity.ok(AnimalType.values());
    }

    @JsonView(Views.Public.class)
    @RequestMapping(value = "/flora", method = RequestMethod.GET)
    public ResponseEntity getFlora() {
        log.info("Received request about flora");
        return ResponseEntity.ok(Flora.values());
    }

    @RequestMapping(value = "/fauna/{xCoord}/{yCoord}/{type}", method = RequestMethod.PUT)
    public ResponseEntity seedLife(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord, @PathVariable("type") String type) {
        log.info("Received request to seed fauna at coordinate: " + xCoord + "x, " + yCoord + "y, with type " + type);
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");
        Assert.notNull(AnimalType.fromName(type), "No type was found for " + type);

        lifeManager.seedLife(xCoord, yCoord, AnimalType.fromName(type));
        log.info("Seeded a " + type + " life at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/flora/{xCoord}/{yCoord}/{type}", method = RequestMethod.PUT)
    public ResponseEntity seedPlant(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord, @PathVariable("type") String type) {
        log.info("Received request to seed flora at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");
        Assert.notNull(Flora.fromName(type), "No type was found for " + type);

        lifeManager.seedFlora(xCoord, yCoord, Flora.fromName(type));
        log.info("Seeded a " + type + " plant at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }
}
