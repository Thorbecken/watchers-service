package com.watchers.boostrap;

import com.watchers.config.SettingConfiguration;
import com.watchers.config.WorldSettingFactory;
import com.watchers.manager.FileSaveManager;
import com.watchers.manager.MapManager;
import com.watchers.manager.SaveToDatabaseManager;
import com.watchers.manager.WorldSettingManager;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.WorldStatusEnum;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.model.world.WorldSettings;
import com.watchers.model.world.WorldTypeEnum;
import com.watchers.service.WorldService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
@SuppressWarnings("unused")
public class WorldBootstrap implements CommandLineRunner {

    private final FileSaveManager fileSaveManager;
    private final WorldService worldService;
    private final MapManager mapManager;
    private final SettingConfiguration settingConfiguration;
    private final WorldSettingManager worldSettingManager;
    private final WorldSettingFactory worldSettingFactory;
    private final SaveToDatabaseManager saveToDatabaseManager;

    @Override
    public void run(String... args) {
        WorldMetaData worldMetaData = worldSettingManager.createNewWorldSetting(1L, WorldStatusEnum.INITIALLIZING, WorldTypeEnum.NON_EUCLIDEAN, false, false, false);
        WorldSettings worldSettings = worldSettingFactory.createWorldSetting();

        if (settingConfiguration.isPersistent()) {
            if (fileSaveManager.exist(1L)) {
                worldService.addActiveWorld(worldMetaData, true);
            } else {
                log.warn("No world was found on startup! Generating a new world.");
                World newWorld = mapManager.createWorld(worldMetaData, worldSettings);

                saveToDatabaseManager.complexSaveToMemory(newWorld, true);
                log.info("Created a new world! Number: " + newWorld.getId());
            }
        } else {
            worldService.addActiveWorld(worldMetaData, false);
        }

        worldSettingManager.setWorldInWaiting(1L);
    }
}
