package com.watchers.model.dto;

import com.watchers.model.world.Continent;
import lombok.Getter;

@Getter
public class MockContinentObject {

    private final Long id;
    private final long size;

    public MockContinentObject(Continent continent) {
        this.id = continent.getId();
        this.size = continent.getCoordinates().size();
    }

}
