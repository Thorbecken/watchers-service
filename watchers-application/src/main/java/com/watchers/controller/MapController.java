package com.watchers.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.manager.MapManager;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.WorldStatusEnum;
import com.watchers.model.world.World;
import com.watchers.repository.WorldSettingsRepository;
import lombok.AllArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@Slf4j
@CrossOrigin
@RestController
@AllArgsConstructor
@Api(description = "This controller exposes functionalitie about retrieving all the worlds.")
public class MapController {

    private MapManager mapManager;
    private WorldSettingsRepository worldSettingsRepository;

    @JsonView(Views.Public.class)
    @RequestMapping(value = "/world/{worldId}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the json of the chosen world.")
    public ResponseEntity<World> getWorldMap(@PathVariable("worldId") Long worldId) {
        Assert.notNull(worldId, "No world id was found");
        if (worldSettingsRepository.existsById(worldId) && !worldSettingsRepository.getOne(worldId).getWorldStatusEnum().equals(WorldStatusEnum.INITIALLIZING)) {
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
