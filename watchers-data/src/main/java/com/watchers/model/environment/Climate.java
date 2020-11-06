package com.watchers.model.environment;

import com.watchers.model.coordinate.Coordinate;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class Climate {
    private long longitude;
    private long latitude;
    private long distanceToEquator;

    private Coordinate coordinate;
}
