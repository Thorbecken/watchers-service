package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.common.Coordinate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "animal")
@EqualsAndHashCode(callSuper=true)
@SequenceGenerator(name="Animal_Gen", sequenceName="Animal_Seq", allocationSize = 1)
public abstract class Animal extends Actor {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="Animal_Gen", sequenceName="Animal_Seq", allocationSize = 1)
    @GeneratedValue(generator="Animal_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "animal_id")
    private Long id;

    static int FORAGING_RANGE = 3;

    private float foodReserve;
    private float maxFoodReserve;
    private float foraging;
    private float metabolisme;
    private float reproductionRate;
    private int movement;
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
                    .filter(coordinate -> this.getNaturalHabitat().movavableSurfaces
                            .contains(coordinate.getTile().getSurfaceType()))
                    .max((coordinate1, coordinate2) -> Math.round(coordinate1.getTile().getBiome().getCurrentFood() - coordinate2.getTile().getBiome().getCurrentFood()))
                    .ifPresent(this::moveToTile);
        }
    }

    private void moveToTile(Coordinate coordinate) {
        getCoordinate().getActors().remove(this);
        setCoordinate(coordinate);
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
            System.out.println("Animal with ID " + id + " is dead but still walking the world");
        }
        this.metabolize();
        if(getStateType() != StateType.DEAD) {
            this.move();
            this.eat();
            this.reproduce();
        }
    }

}
