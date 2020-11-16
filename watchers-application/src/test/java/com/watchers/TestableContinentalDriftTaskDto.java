package com.watchers;

import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import org.springframework.util.Assert;

public class TestableContinentalDriftTaskDto {

    public static ContinentalDriftTaskDto createContinentalDriftTaskDto(World world){
        ContinentalDriftTaskDto  taskDto = new ContinentalDriftTaskDto(world.getId(), false, true, 2, 5);

        Assert.isTrue(world.getId().equals(taskDto.getWorldId()), "world was nog set");
        Assert.isTrue(taskDto.getHeightDivider() == 2, "heightDefider not set");
        Assert.isTrue(taskDto.getMinContinents() == 5, "minimalcontintinents not set");

        return taskDto;
    }
}
