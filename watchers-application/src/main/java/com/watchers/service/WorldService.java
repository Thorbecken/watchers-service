package com.watchers.service;

import com.watchers.manager.*;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@EnableTransactionManagement
public class WorldService {

    private WorldRepository worldRepository;
    private FileSaveManager fileSaveManager;
    private SaveToDatabaseManager saveToDatabaseManager;
    private MapManager mapManager;
    private ContinentalDriftManager continentalDriftManager;
    private CleansingManager cleansingManager;
    private LifeManager lifeManager;
    private ArrayList<Long> activeWorldIds;

    public void saveWorld(World memoryWorld){
        boolean exists = fileSaveManager.exist(memoryWorld.getId());
        if (!exists){
            fileSaveManager.saveWorld(memoryWorld);
            log.warn("The missing world is now saved to persistence.");
        } else {
            log.info("World is beeing updated from memory.");
            fileSaveManager.saveWorld(memoryWorld);
        }
    }

    public void processTurn(WorldTaskDto worldTaskDto){
        log.trace(getTotalHeight(worldTaskDto.getWorldId()));
        if(worldTaskDto.isContinentalshift()) {
            Assert.isTrue(worldTaskDto instanceof ContinentalDriftTaskDto, "The WorldTaskDto was initiated wrongly");
            continentalDriftManager.process((ContinentalDriftTaskDto) worldTaskDto);
            cleansingManager.process(worldTaskDto);
        }

        lifeManager.process(worldTaskDto);
        cleansingManager.process(worldTaskDto);

        if(worldTaskDto.isSaving()){
            fileSaveManager.saveWorld(worldTaskDto);
        }
    }

    @Transactional
    private String getTotalHeight(@NonNull Long worldId) {
        World world = worldRepository.findById(worldId).get();
        long currentHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .mapToLong(Tile::getHeight)
                .sum();
        long totalHeight = currentHeight + world.getHeightDeficit();
        return "Current height is: " +  totalHeight + ".";
    }

    /**
     * @param id the id of the world to be added
     * @param startFromPersistents boolean value for the use of the persistent database
     * @return Boolean: true means added, null means already added false means not present in memory
     */
    public Boolean addActiveWorld(Long id, boolean startFromPersistents) {
        if (startFromPersistents){
            return addActiveWorldFromPersistence(id);
        } else {
            return addActiveWorldFromMemory(id);
        }
    }

    private Boolean addActiveWorldFromPersistence(Long id) {
        Optional<World> optionalWorld = fileSaveManager.findById(id);
        if(optionalWorld.isPresent()) {
            World world = optionalWorld.get();
            saveToMemory(world);
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                log.info("World " + id + " added as active world from the persistence database.");
                return true;
            } else {
                log.info("World " + id + " added as active world from the persistence database.");
                return null;
            }
        } else {
            log.info("World " + id + " was not present in the persistence database.");
            return false;
        }
    }

    @Transactional
    private void saveToMemory(World persistentWorld) {
        saveToDatabaseManager.complexSaveToMemory(persistentWorld);

        World newWorld = worldRepository.findById(persistentWorld.getId()).orElseThrow(() -> new RuntimeException("The world was lost in perstistence."));
        log.info("current coordinates from memory are: " + newWorld.getCoordinates().size());
        log.info("current coordinates from memory are: " + persistentWorld.getCoordinates().size());
    }

    private Boolean addActiveWorldFromMemory(Long id) {
        if (worldRepository.existsById(id)) {
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                log.info("The requested world " + id + " is now active.");
                return true;
            } else {
                log.info("The requested world " + id + " was alreadu active.");
                return null;
            }
        } else {
            log.warn("The world " + id + " does not exist in the persistence context. A new world is going to be created. Large worlds take a while being generated.");
            World world = mapManager.createWorld();
            worldRepository.save(world);
            log.info("Created a new world! Number: " + world.getId());
            activeWorldIds.add(id);
            return true;
        }
    }
}
