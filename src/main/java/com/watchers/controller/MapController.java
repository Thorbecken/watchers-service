package com.watchers.controller;

import com.watchers.manager.MapManager;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class MapController {

    @Autowired
    private MapManager mapManager;

    @RequestMapping(value = "/world/{worldId}", method = RequestMethod.GET)
    public ResponseEntity<World> getWorldMap(@PathVariable("worldId") Long worldId){
        Assert.notNull(worldId, "No world id was found");

        World world = mapManager.getWorld(worldId);
        System.out.println("Returned a world");
        return ResponseEntity.ok(world);
    }
}
