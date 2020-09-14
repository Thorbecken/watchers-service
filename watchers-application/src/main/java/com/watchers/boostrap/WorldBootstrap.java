package com.watchers.boostrap;

import com.watchers.manager.MapManager;
import com.watchers.model.environment.World;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import com.watchers.service.WorldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("unused")
public class WorldBootstrap implements CommandLineRunner {

    private final WorldRepositoryPersistent worldRepositoryPersistent;
    private final WorldService worldService;
    private final MapManager mapManager;
    private final boolean persistent;

    @SuppressWarnings("unused")
    public WorldBootstrap(WorldRepositoryPersistent worldRepositoryPersistent, WorldService worldService, MapManager mapManager, @Value("${startup.persistent}") boolean persistent) {
        this.worldRepositoryPersistent = worldRepositoryPersistent;
        this.worldService = worldService;
        this.mapManager = mapManager;
        this.persistent = persistent;
    }

    @Override
    public void run(String... args) {
        if(persistent){
            if(worldRepositoryPersistent.existsById(1L)){
                worldService.addActiveWorld(1L, true);
            } else {
                log.warn("No world was found on startup! Generating a new world.");
                World newWorld = mapManager.getWorld(1L, false);
                worldService.saveWorld(newWorld);
            }
        } else {
            worldService.addActiveWorld(1L, false);
        }
    }
}
