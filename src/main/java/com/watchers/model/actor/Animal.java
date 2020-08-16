package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.environment.Tile;
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

    public abstract void generateOffspring(Tile tile, float foodPassed);

    private void metabolize(){
        if(metabolisme > foodReserve){
            setStateType(StateType.DEAD);
        } else {
            foodReserve = foodReserve - metabolisme;
        }
    }

    private void move(){
        float localFood = getTile().getBiome().getCurrentFood();
        if (localFood < foraging) {
            getTile().getNeighbours().stream()
                    .filter(tile -> this.getNaturalHabitat().movavableSurfaces
                            .contains(tile.getSurfaceType()))
                    .max((tile1, tile2) -> Math.round(tile1.getBiome().getCurrentFood() - tile2.getBiome().getCurrentFood()))
                    .ifPresent(this::moveToTile);
        }
    }

    private void moveToTile(Tile tile) {
        getTile().getActors().remove(this);
        setTile(tile);
        getTile().getActors().add(this);
    }

    private void eat(){
        float localFood = getTile().getBiome().getCurrentFood();
        if(localFood >= foraging){
            foodReserve = foodReserve + foraging;
            getTile().getBiome().setCurrentFood(localFood - foraging);
        }
    }

    private void reproduce(){
        if(reproductionRate <= (foodReserve / maxFoodReserve)){
            generateOffspring(getTile(), foodReserve/2);
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
