package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.SerialTask;
import com.watchers.model.coordinate.Coordinate;
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

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private Coordinate coordinate;


    @Enumerated(value = EnumType.STRING)
    private StateType stateType;
    @Enumerated(value = EnumType.STRING)
    private NaturalHabitat naturalHabitat;

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
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
