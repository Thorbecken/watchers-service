package com.watchers.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.manager.MapManager;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.WorldStatusEnum;
import com.watchers.model.world.World;
import com.watchers.repository.WorldMetaDataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@Slf4j
@CrossOrigin
@RestController
@AllArgsConstructor
public class MapController {

    private final MapManager mapManager;
    private final WorldMetaDataRepository worldMetaDataRepository;

    @JsonView(Views.Public.class)
    @RequestMapping(value = "/world/{worldId}", method = RequestMethod.GET)
    public ResponseEntity<World> getWorldMap(@PathVariable("worldId") Long worldId) {
        Assert.notNull(worldId, "No world id was found");
        if (worldMetaDataRepository.existsById(worldId) && !worldMetaDataRepository.getOne(worldId).getWorldStatusEnum().equals(WorldStatusEnum.INITIALLIZING)) {
            World world = mapManager.getUninitiatedWorld(worldId);
            if (world != null) {
                log.info(generateGetWorldMapLogMessage(world));
                return ResponseEntity.ok(world);
            }
        }

        return null;
    }

    private String generateGetWorldMapLogMessage(World world) {
        return "Returned a world with " + world.getCoordinates().stream().map(Coordinate::getActors).reduce(new HashSet<>(), (x, y) -> {
            x.addAll(y);
            return x;
        }).size() + " actors in it";

    }
}
