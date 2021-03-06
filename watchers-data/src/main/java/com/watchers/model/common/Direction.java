package com.watchers.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@JsonSerialize
@NoArgsConstructor
@Table(name = "directions")
public class Direction {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("directionId")
    @SequenceGenerator(name="Direction_Gen", sequenceName="Direction_Seq", allocationSize = 1)
    @GeneratedValue(generator="Direction_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "direction_id")
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "x_velocity")
    private int xVelocity;

    @JsonView(Views.Public.class)
    @Column(name = "y_velocity")
    private int yVelocity;

    public Direction(int xVelocity, int yVelocity){
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    public Direction createClone() {
        Direction clone = new Direction();
        clone.setId(this.id);
        clone.setXVelocity(this.xVelocity);
        clone.setYVelocity(this.yVelocity);
        return  clone;
    }
}
