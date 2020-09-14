package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.SerialTask;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.World;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "actor")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name="Actor_Gen", sequenceName="Actor_Seq", allocationSize = 1)
public abstract class Actor implements SerialTask {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="Actor_Gen", sequenceName="Actor_Seq", allocationSize = 1)
    @GeneratedValue(generator="Actor_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "actor_id")
    private Long id;

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "tile_id")
    private Coordinate coordinate;


    private StateType stateType;
    private NaturalHabitat naturalHabitat;

    public abstract void processSerialTask();
    public abstract void handleContinentalMovement();
    public boolean isCorrectLandType(Coordinate coordinate){
        return this.getNaturalHabitat().movableSurfaces
                .contains(coordinate.getTile().getSurfaceType());
    }
    public boolean isOnCorrectLand(){
        return isCorrectLandType(coordinate);
    }
    public boolean isNotOnCorrectLand(){
        return !isOnCorrectLand();
    }

    public abstract Actor createClone(Coordinate newCoordinate);
}
