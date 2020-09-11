package com.watchers.boostrap;

import com.watchers.service.WorldService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class WorldBootstrap implements CommandLineRunner {

    private final WorldService worldService;
    private final boolean persistent;

    @SuppressWarnings("unused")
    public WorldBootstrap(WorldService worldService, @Value("${startup.persistent}") boolean persistent) {
        this.worldService = worldService;
        this.persistent = persistent;
    }

    @Override
    public void run(String... args) {
        if(persistent){
            Boolean worldStarted = worldService.addActiveWorld(1L, true);
            if(!worldStarted){
                worldService.saveWorld(1L);
            }
        } else {
            worldService.addActiveWorld(1L, false);
        }
    }
}
