package com.watchers.model.actors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.StateType;
import com.watchers.model.environment.Biome;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Slf4j
@Entity
@Table(name = "animal")
@SequenceGenerator(name = "Animal_Gen", sequenceName = "Animal_Seq", allocationSize = 1)
public class Animal extends Actor {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("animalId")
    @SequenceGenerator(name = "Animal_Gen", sequenceName = "Animal_Seq", allocationSize = 1)
    @GeneratedValue(generator = "Animal_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "animal_id")
    private Long id;

    @Transient
    @JsonIgnore
    static int FORAGING_RANGE = 3;

    @JsonView(Views.Public.class)
    @Column(name = "food_reserve")
    private float foodReserve;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    @Column(name = "animal_type")
    private AnimalType animalType;

    @SuppressWarnings("unused")
    private Animal() {
    }

    public void generateOffspring(Coordinate coordinate, float foodPassed) {
        coordinate.getActors().add(new Animal(coordinate, this.animalType, foodPassed));
    }

    private void metabolize() {
        if (animalType.getMetabolisme() > foodReserve) {
            setStateType(StateType.DEAD);
        } else {
            foodReserve = foodReserve - animalType.getMetabolisme();
        }
    }

    private void move() {
        double localFood = getCoordinate().getTile().getBiome().getCurrentFood();
        if (localFood < animalType.getForaging()) {
            getCoordinate().getNeighbours().stream()
                    .filter(coordinate -> this.getNaturalHabitat().movableSurfaces
                            .contains(coordinate.getTile().getSurfaceType()))
                    .max((coordinate1, coordinate2) -> Math.toIntExact(Math.round(coordinate1.getTile().getBiome().getCurrentFood() - coordinate2.getTile().getBiome().getCurrentFood())))
                    .ifPresent(this::moveToTile);
        }
    }

    private void moveToTile(Coordinate newCoordinate) {
        getCoordinate().getActors().remove(this);
        setCoordinate(newCoordinate);
        getCoordinate().getActors().add(this);
    }

    private void eat() {
        Biome biome = getCoordinate().getTile().getBiome();
        double localFood = biome.getCurrentFood();
        if (localFood >= animalType.getForaging()) {
            foodReserve = foodReserve + animalType.getForaging();
            biome.forage(animalType.getForaging());
        }
    }

    private void reproduce() {
        if (animalType.getReproductionRate() <= (foodReserve / animalType.getMaxFoodReserve())) {
            generateOffspring(getCoordinate(), foodReserve / 2);
            foodReserve = foodReserve / 2;
        }
    }

    @Override
    public void processSerialTask() {
        if (StateType.DEAD.equals(getStateType())) {
            log.trace("Animal with ID " + id + " is dead but still walking the world");
        }
        this.metabolize();
        if (getStateType() != StateType.DEAD) {
            this.move();
            this.eat();
            this.reproduce();
        }
    }

    public void handleContinentalMovement() {
        Optional<Coordinate> optionalCoordinate = getCoordinate().getNeighbours().stream()
                .filter(this::isCorrectLandType)
                .max((coordinate1, coordinate2) -> Math.toIntExact(Math.round(coordinate1.getTile().getBiome().getCurrentFood() - coordinate2.getTile().getBiome().getCurrentFood())));
        if (optionalCoordinate.isPresent()) {
            moveToTile(optionalCoordinate.get());
        } else {
            setStateType(StateType.DEAD);
        }
    }

    @Override
    public Actor createClone(Coordinate newCoordinate) {
        return new Animal(newCoordinate, this.animalType, this.foodReserve);
    }

    public Animal(Coordinate coordinate, AnimalType animalType, float StartingFood) {
        super.setActorType(ActorType.ANIMAL);
        super.setNaturalHabitat(animalType.getNaturalHabitat());

        setFoodReserve(StartingFood);
        setAnimalType(animalType);
        setStateType(StateType.ALIVE);
        setCoordinate(coordinate);

        List<Actor> newActors = getCoordinate().getWorld().getNewActors();
        if (newActors != null) {
            long id;
            if (newActors.isEmpty()) {
                id = coordinate.getWorld().getActorList().stream()
                        .mapToLong(Actor::getId)
                        .max()
                        .orElse(0L);
            } else {
                id = newActors.stream()
                        .mapToLong(Actor::getId)
                        .max()
                        .orElseThrow();
            }
            setId(id + 1L);
            newActors.add(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        if (!super.equals(o)) return false;
        Animal animal = (Animal) o;
        return Objects.equals(id, animal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
