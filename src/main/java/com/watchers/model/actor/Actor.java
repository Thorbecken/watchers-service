package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.SerialTask;
import com.watchers.model.environment.Tile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
public abstract class Actor implements SerialTask {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="Continent_Gen", sequenceName="Continent_Seq", allocationSize = 1)
    @GeneratedValue(generator="Continent_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "continent_id")
    private Long id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "tile_id", nullable = false)
    private Tile tile;


    private StateType stateType;
    private NaturalHabitat naturalHabitat;

    public abstract void processSerialTask();
}
