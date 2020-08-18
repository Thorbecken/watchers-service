package com.watchers.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SaveJobService {

    private WorldService worldService;

    public SaveJobService(WorldService worldService) {
        this.worldService = worldService;
    }

    public void executeSave() {
        //worldService.saveWorlds();
        //log.info("Saved the worlds");
    }

}
