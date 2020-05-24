package com.watchers.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @SequenceGenerator(name="Direction_Gen", sequenceName="Direction_Seq", allocationSize = 1)
    @GeneratedValue(generator="Direction_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "direction_id")
    private int id;

    @JsonIgnore
    @Column(name = "x_velocity")
    private int xVelocity;

    @JsonIgnore
    @Column(name = "y_velocity")
    private int yVelocity;

    public Direction(int xVelocity, int yVelocity){
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }
}
