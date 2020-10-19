package com.watchers.controller;

import com.watchers.manager.MapManager;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.World;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@CrossOrigin
@RestController
public class MapController {

    private MapManager mapManager;

    public MapController(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    @RequestMapping(value = "/world/{worldId}", method = RequestMethod.GET)
    public ResponseEntity<World> getWorldMap(@PathVariable("worldId") Long worldId){
        Assert.notNull(worldId, "No world id was found");
        World world = mapManager.getUninitiatedWorld(worldId);
        if (world != null) {
            System.out.println("Returned a world with " + world.getCoordinates().stream().map(Coordinate::getActors).reduce(new HashSet<>(), (x, y) -> {
                x.addAll(y);
                return x;
            }).size() + " actors in it");
            return ResponseEntity.ok(world);
        } else {
            return null;
        }
    }
}
