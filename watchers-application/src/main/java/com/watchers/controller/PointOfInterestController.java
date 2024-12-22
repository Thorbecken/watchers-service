package com.watchers.controller;

import com.watchers.manager.PointOfInterestManager;
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
public class PointOfInterestController {
    private PointOfInterestManager pointOfInterestManager;
    private WorldRepository worldRepository;

    @RequestMapping(value = "/hotspot/{xCoord}/{yCoord}", method = RequestMethod.PUT)
    public ResponseEntity addHotSpot(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to create a hotspot at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        pointOfInterestManager.addHotspot(xCoord, yCoord);
        log.info("Create a hotspot at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/tectonicPlume/{xCoord}/{yCoord}", method = RequestMethod.PUT)
    public ResponseEntity addTectonicPlume(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to create a tectonic plume at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        pointOfInterestManager.addTectonicPlume(xCoord, yCoord);
        log.info("Created a tectonic plume at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/aquifer/{xCoord}/{yCoord}", method = RequestMethod.PUT)
    public ResponseEntity addAquifer(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to create a aquifer at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        pointOfInterestManager.addAquifer(xCoord, yCoord);
        log.info("Created a aquifer at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{xCoord}/{yCoord}", method = RequestMethod.DELETE)
    public ResponseEntity removePointOfInterest(@PathVariable("xCoord") Long xCoord, @PathVariable("yCoord") Long yCoord) {
        log.info("Received request to delete a point of interest at coordinate: " + xCoord + "x, " + yCoord + "y");
        Assert.notNull(xCoord, "No xCoord was found");
        Assert.notNull(yCoord, "No yCoord was found");

        String removedPointOfInterest = pointOfInterestManager.removePointOfInterest(xCoord, yCoord);
        log.info("Deleted a " + removedPointOfInterest + " at coordinates: " + xCoord + "x, " + yCoord + "y");
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/greatFlora/{xCoordinate}/{yCoordinate}/{type}", method = RequestMethod.PUT)
    public ResponseEntity seedTreeSpirit(@PathVariable("xCoordinate") Long xCoordinate, @PathVariable("yCoordinate") Long yCoordinate, @PathVariable("type") String type) {
        log.info("Received request to seed great flora at coordinate: " + xCoordinate + "x, " + yCoordinate + "y");
        Assert.notNull(xCoordinate, "No xCoordinate was found");
        Assert.notNull(yCoordinate, "No yCoordinate was found");
        Assert.notNull(Flora.fromName(type), "No type was found for " + type);

        pointOfInterestManager.plantGreatFlora(xCoordinate, yCoordinate, Flora.fromName(type));
        log.info("Seeded a " + type + " great flora at coordinates: " + xCoordinate + "x, " + yCoordinate + "y");
        return ResponseEntity.ok().build();
    }


}
