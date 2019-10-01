package com.watchers.controller;

import com.watchers.manager.MapManager;
import com.watchers.model.World;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MapController {

    @Autowired
    private MapManager mapManager;

    @RequestMapping(value = "/world/{worldId}", method = RequestMethod.GET)
    public ResponseEntity<World> getWorldMap(@PathVariable("worldId") Long worldId){
        World world = mapManager.getWorld(worldId);
        return ResponseEntity.ok(world);
    }
}
