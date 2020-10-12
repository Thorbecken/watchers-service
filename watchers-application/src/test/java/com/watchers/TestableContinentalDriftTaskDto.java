package com.watchers;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;

public class TestableContinentalDriftTaskDto {

    public static ContinentalDriftTaskDto createContinentalDriftTaskDto(World world){
        ContinentalDriftTaskDto  taskDto = new ContinentalDriftTaskDto(world.getId());

        taskDto.setWorldId(world.getId());
        taskDto.setHeightDivider(2);
        taskDto.setMinContinents(5);

        return taskDto;
    }
}
