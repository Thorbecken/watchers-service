package com.watchers.service;

import com.watchers.config.WorldSettingFactory;
import com.watchers.manager.*;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.Watershed;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class WorldService {

    private final WorldRepository worldRepository;
    private final FileSaveManager fileSaveManager;
    private final SaveToDatabaseManager saveToDatabaseManager;
    private final MapManager mapManager;
    private final ContinentalDriftManager continentalDriftManager;
    private final ClimateManager climateManager;
    private final CleansingManager cleansingManager;
    private final LifeManager lifeManager;
    private final ArrayList<Long> activeWorldIds;
    private final WorldSettingFactory worldSettingFactory;

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
        if(worldTaskDto instanceof ContinentalDriftTaskDto) {
            continentalDriftManager.process((ContinentalDriftTaskDto) worldTaskDto);
            climateManager.proces(worldTaskDto);
            cleansingManager.process(worldTaskDto);
        } else {
            climateManager.proces(worldTaskDto);
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
     * @param worldMetaData the worldsettings of the world
     * @param startFromPersistents boolean value for the use of the persistent database
     * @return Boolean: true means added, null means already added false means not present in memory
     */
    public Boolean addActiveWorld(WorldMetaData worldMetaData, boolean startFromPersistents) {
        if (startFromPersistents){
            return addActiveWorldFromPersistence(worldMetaData);
        } else {
            return addActiveWorldFromMemory(worldMetaData);
        }
    }

    private Boolean addActiveWorldFromPersistence(WorldMetaData worldMetaData) {
        Optional<World> optionalWorld = fileSaveManager.findById(worldMetaData.getId());
        if(optionalWorld.isPresent()) {
            World world = optionalWorld.get();
            saveToMemory(world);
            if (!activeWorldIds.contains(worldMetaData.getId())) {
                activeWorldIds.add(worldMetaData.getId());
                log.info("World " + worldMetaData.getId() + " added as active world from the persistence database.");
                return true;
            } else {
                log.info("World " + worldMetaData.getId() + " added as active world from the persistence database.");
                return null;
            }
        } else {
            log.info("World " + worldMetaData.getId() + " was not present in the persistence database.");
            return false;
        }
    }

    private void saveToMemory(World persistentWorld) {
        saveToDatabaseManager.complexSaveToMemory(persistentWorld);

        log.info("Loaded " + persistentWorld.getCoordinates().size() + " coordinates");
        log.info("Loaded " + persistentWorld.getContinents().size() + " continents");
        log.info("Loaded " + persistentWorld.getActorList().size() + " actors");
        log.info("Loaded " + persistentWorld.getCoordinates().stream().map(Coordinate::getTile).map(Tile::getWatershed).distinct().count() + " watersheds");
        log.info("Loaded " + persistentWorld.getCoordinates().stream().map(Coordinate::getTile).map(Tile::getRiver).count() + " rivers");
    }

    private Boolean addActiveWorldFromMemory(WorldMetaData worldMetaData) {
        Long id = worldMetaData.getId();
        if (worldRepository.existsById(id)) {
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                log.info("The requested world " + id + " is now active.");
                return true;
            } else {
                log.info("The requested world " + worldMetaData.getId() + " was alreadu active.");
                return null;
            }
        } else {
            log.warn("The world " + id + " does not exist in the persistence context. A new world is going to be created. Large worlds take a while being generated.");
            World world = mapManager.createWorld(worldMetaData, worldSettingFactory.createWorldSetting());
            saveToDatabaseManager.complexSaveToMemory(world, true);
            log.info("Created a new world! Number: " + world.getId());
            activeWorldIds.add(worldMetaData.getId());
            return true;
        }
    }
}
