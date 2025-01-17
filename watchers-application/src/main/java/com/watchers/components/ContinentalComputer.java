package com.watchers.components;

import com.watchers.model.dto.ContinentalDriftTaskDto;

public interface ContinentalComputer extends Computer<ContinentalDriftTaskDto> {

    void process(ContinentalDriftTaskDto continentalDriftTaskDto);

}
