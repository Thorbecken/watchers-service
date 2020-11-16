package com.watchers.model.actors;

import com.fasterxml.jackson.annotation.*;
import com.watchers.model.actors.animals.Rabbit;
import com.watchers.model.actors.animals.Whale;
import com.watchers.model.common.Coordinate;
import com.watchers.model.common.Views;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.StateType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Optional;

@Data
@Slf4j
@Entity
@Table(name = "animal")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="animal_type",
        discriminatorType = DiscriminatorType.STRING)
@EqualsAndHashCode(callSuper=true)
@SequenceGenerator(name="Animal_Gen", sequenceName="Animal_Seq", allocationSize = 1)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Rabbit.class, name = "rabbit"),
        @JsonSubTypes.Type(value = Whale.class, name = "whale")
})
public abstract class Animal extends Actor {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("animalId")
    @SequenceGenerator(name="Animal_Gen", sequenceName="Animal_Seq", allocationSize = 1)
    @GeneratedValue(generator="Animal_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "animal_id")
    private Long id;

    @Transient
    @JsonIgnore
    static int FORAGING_RANGE = 3;

    @JsonView(Views.Public.class)
    @Column(name = "food_reserve")
    private float foodReserve;

    @JsonView(Views.Public.class)
    @Column(name = "max_food")
    private float maxFoodReserve;

    @JsonView(Views.Public.class)
    @Column(name = "foraging")
    private float foraging;

    @JsonView(Views.Public.class)
    @Column(name = "metabolism")
    private float metabolisme;

    @JsonView(Views.Public.class)
    @Column(name = "reproduction_rate")
    private float reproductionRate;

    @JsonView(Views.Public.class)
    @Column(name = "movement")
    private int movement;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    @Column(name = "animal_type")
    private AnimalType animalType;

    public abstract void generateOffspring(Coordinate coordinate, float foodPassed);

    private void metabolize(){
        if(metabolisme > foodReserve){
            setStateType(StateType.DEAD);
        } else {
            foodReserve = foodReserve - metabolisme;
        }
    }

    private void move(){
        float localFood = getCoordinate().getTile().getBiome().getCurrentFood();
        if (localFood < foraging) {
            getCoordinate().getNeighbours().stream()
                    .filter(coordinate -> this.getNaturalHabitat().movableSurfaces
                            .contains(coordinate.getTile().getSurfaceType()))
                    .max((coordinate1, coordinate2) -> Math.round(coordinate1.getTile().getBiome().getCurrentFood() - coordinate2.getTile().getBiome().getCurrentFood()))
                    .ifPresent(this::moveToTile);
        }
    }

    private void moveToTile(Coordinate newCoordinate) {
        getCoordinate().getActors().remove(this);
        setCoordinate(newCoordinate);
        getCoordinate().getActors().add(this);
    }

    private void eat(){
        float localFood = getCoordinate().getTile().getBiome().getCurrentFood();
        if(localFood >= foraging){
            foodReserve = foodReserve + foraging;
            getCoordinate().getTile().getBiome().setCurrentFood(localFood - foraging);
        }
    }

    private void reproduce(){
        if(reproductionRate <= (foodReserve / maxFoodReserve)){
            generateOffspring(getCoordinate(), foodReserve/2);
            foodReserve = foodReserve/2;
        }
    }

    @Override
    public void processSerialTask() {
        if(StateType.DEAD.equals(getStateType())){
            log.trace("Animal with ID " + id + " is dead but still walking the world");
        }
        this.metabolize();
        if(getStateType() != StateType.DEAD) {
            this.move();
            this.eat();
            this.reproduce();
        }
    }

    public void handleContinentalMovement(){
        Optional<Coordinate> optionalCoordinate = getCoordinate().getNeighbours().stream()
                .filter(this::isCorrectLandType)
                .max((coordinate1, coordinate2) -> Math.round(coordinate1.getTile().getBiome().getCurrentFood() - coordinate2.getTile().getBiome().getCurrentFood()));
        if (optionalCoordinate.isPresent()){
            moveToTile(optionalCoordinate.get());
        } else {
            setStateType(StateType.DEAD);
        }
    }

    private void setSuperId(Long id){
        super.setId(id);
    }

    public Animal cloneBasis(Animal clone, Coordinate newCoordinate){
        clone.setId(this.getId());
        clone.setSuperId(this.getId());
        clone.setCoordinate(newCoordinate);
        clone.setStateType(this.getStateType());
        clone.setNaturalHabitat(this.getNaturalHabitat());

        clone.setFoodReserve(this.getFoodReserve());
        clone.setFoodReserve(this.getMaxFoodReserve());
        clone.setForaging(this.getForaging());
        clone.setMetabolisme(this.getMetabolisme());
        clone.setReproductionRate(this.getReproductionRate());
        clone.setMovement(this.getMovement());
        clone.setAnimalType(this.getAnimalType());

        return clone;
    }
}
