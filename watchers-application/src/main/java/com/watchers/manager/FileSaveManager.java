package com.watchers.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchers.config.SettingConfiguration;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.common.Views;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class FileSaveManager {

    private final WorldRepository worldRepository;
    private final WorldSettingManager worldSettingManager;
    private final SettingConfiguration settingConfiguration;

    @Transactional
    public void saveWorld(WorldTaskDto taskDto) {
        StopwatchTimer.start();
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        save(world);
        worldSettingManager.changeSaveSetting(taskDto.getWorldId(), false);
        StopwatchTimer.stop("saveWorld");
    }
    public void saveWorld(World world){
        save(world);
    }

    public boolean exist(@NonNull Long aAlong) {
        return getWorldFile(aAlong.toString()).exists();
    }

    public Optional<World> findById(@NonNull Long id) {
        try {
            World world = load(id);
            return Optional.of(world);
        } catch (IOException ex) {
            log.warn("World " + id.toString() + " was reequested to be loaded but an exception occured: " + ex);
        }
        return Optional.empty();
    }

    private void save(@NonNull World world) {
        String id = world.getId().toString();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Writing to a file
            mapper.writerWithView(Views.Internal.class).writeValue(getWorldFile(id), world);
            log.warn("saved world " + world.getId() + " to file on director: " + getWorldFile(id) + ".");

        } catch (IOException e) {
            log.warn(e.toString());
        }
    }

    private File getWorldFile(String id) {
        return new File(settingConfiguration.getDirectory() + settingConfiguration.getPrefix() + "world_" + id + ".json");
    }

    private World load(@NonNull Long aLong) throws IOException {
        File file = getWorldFile(aLong.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readerWithView(Views.Internal.class).readValue(file, World.class);
    }
}
