package com.watchers.job;

import com.watchers.controller.TurnController;
import com.watchers.manager.MapManager;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnJobService {

    @Autowired
    private MapManager mapManager;

    @Autowired
    private WorldRepository worldRepository;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void executeSampleJob() {
        World world = mapManager.getWorld(1L);
        TurnController.processTurn(world);
        worldRepository.save(world);
        logger.info("Processed a turn");

    }

}
