package com.watchers;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import org.springframework.util.Assert;

public class TestableContinentalDriftTaskDto {

    public static ContinentalDriftTaskDto createContinentalDriftTaskDto(World world){
        ContinentalDriftTaskDto  taskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        taskDto.setWorld(world);
        taskDto.setContinentalshift(true);
        taskDto.setSaving(false);

        Assert.notNull(world.getId(), "world was not set properly");
        Assert.notNull(taskDto.getWorldId(), "world was not set properly");
        Assert.isTrue(world.getId().equals(taskDto.getWorldId()), "world was not set properly");

        return taskDto;
    }
}
