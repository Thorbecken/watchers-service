package com.watchers;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import org.springframework.util.Assert;

public class TestableContinentalDriftTaskDto {

    public static ContinentalDriftTaskDto createContinentalDriftTaskDto(World world){
        ContinentalDriftTaskDto  taskDto = new ContinentalDriftTaskDto(world.getId(), false, true);

        Assert.isTrue(world.getId().equals(taskDto.getWorldId()), "world was nog set");

        return taskDto;
    }
}
