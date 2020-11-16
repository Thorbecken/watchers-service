package com.watchers.model.actors;

import com.fasterxml.jackson.annotation.*;
import com.watchers.model.interfaces.SerialTask;
import com.watchers.model.common.Coordinate;
import com.watchers.model.common.Views;
import com.watchers.model.enums.NaturalHabitat;
import com.watchers.model.enums.StateType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "actor")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name="Actor_Gen", sequenceName="Actor_Seq", allocationSize = 1)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Animal.class, name = "animal")
})
public abstract class Actor implements SerialTask {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("actorId")
    @SequenceGenerator(name="Actor_Gen", sequenceName="Actor_Seq", allocationSize = 1)
    @GeneratedValue(generator="Actor_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "actor_id")
    private Long id;

    @ManyToOne
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Coordinate coordinate;


    @JsonProperty("statusType")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status_type")
    private StateType stateType;

    @JsonProperty("naturalHabitat")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    @Column(name = "natural_habitat")
    private NaturalHabitat naturalHabitat;

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
    public abstract void processSerialTask();
    public abstract void handleContinentalMovement();
    @JsonIgnore
    public boolean isCorrectLandType(Coordinate coordinate){
        return this.getNaturalHabitat().movableSurfaces
                .contains(coordinate.getTile().getSurfaceType());
    }
    @JsonIgnore
    public boolean isOnCorrectLand(){
        return isCorrectLandType(coordinate);
    }
    @JsonIgnore
    public boolean isNotOnCorrectLand(){
        return !isOnCorrectLand();
    }

    public abstract Actor createClone(Coordinate newCoordinate);
}
