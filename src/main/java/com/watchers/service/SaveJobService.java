package com.watchers.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaveJobService {

    @Autowired
    private WorldService worldService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void executeSave() {
        worldService.saveWorlds();
        logger.info("Saved the worlds");
    }

}
