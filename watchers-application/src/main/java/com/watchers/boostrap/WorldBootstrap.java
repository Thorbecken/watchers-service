package com.watchers.boostrap;

import com.watchers.config.SettingConfiguration;
import com.watchers.manager.MapManager;
import com.watchers.manager.WorldSettingManager;
import com.watchers.model.WorldStatusEnum;
import com.watchers.model.environment.World;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import com.watchers.service.WorldService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@SuppressWarnings("unused")
public class WorldBootstrap implements CommandLineRunner {

    private final FileSaveManager fileSaveManager;
    private final WorldService worldService;
    private final MapManager mapManager;
    private final WorldRepository worldRepository;
    private final SettingConfiguration settingConfiguration;
    private final WorldSettingManager worldSettingManager;

    @Override
    public void run(String... args) {
        WorldSetting worldSetting = worldSettingManager.createNewWorldSetting(1L, WorldStatusEnum.INITIALLIZING, WorldTypeEnum.NON_EUCLIDEAN, false, false, false, settingConfiguration.getHeigtDivider(), settingConfiguration.getMinimumContinents());

        if(settingConfiguration.isPersistent()){
            if(fileSaveManager.exist(1L)){
                worldService.addActiveWorld(1L, true);
            } else {
                log.warn("No world was found on startup! Generating a new world.");
                World newWorld = mapManager.createWorld();
                worldRepository.saveAndFlush(newWorld);
                worldService.saveWorld(newWorld);
                log.info("Created a new world! Number: " + newWorld.getId());
            }
        } else {
            worldService.addActiveWorld(worldSetting, false);
        }

        worldSettingManager.setWorldInWaiting(1L);
    }
}
