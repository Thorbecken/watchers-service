package com.watchers.model.actors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.NaturalHabitat;
import com.watchers.model.enums.StateType;
import com.watchers.model.interfaces.SerialTask;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Data
@Entity
@Table(name = "actor")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "actor_type", discriminatorType = DiscriminatorType.STRING)
@SequenceGenerator(name = "Actor_Gen", sequenceName = "Actor_Seq", allocationSize = 1)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Animal.class, name = "Animal")
})
public abstract class Actor implements SerialTask {

    @Id
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "Actor_Gen", sequenceName = "Actor_Seq", allocationSize = 1)
    @GeneratedValue(generator = "Actor_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "actor_id")
    private Long id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private Coordinate coordinate;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private StateType stateType;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private NaturalHabitat naturalHabitat;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private ActorType actorType;

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public abstract void processSerialTask();

    @Transactional
    public abstract void handleContinentalMovement();

    @JsonIgnore
    public boolean isCorrectLandType(Coordinate coordinate) {
        if(coordinate == null){
            return false;
        }
        return this.getNaturalHabitat().movableSurfaces
                .contains(coordinate.getTile().getSurfaceType());
    }

    @JsonIgnore
    public boolean isOnCorrectLand() {
        return isCorrectLandType(coordinate);
    }

    @JsonIgnore
    public boolean isNotOnCorrectLand() {
        return !isOnCorrectLand();
    }

    public abstract Actor createClone(Coordinate newCoordinate);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return Objects.equals(id, actor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
