package com.watchers.model.actor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.watchers.helper.RandomHelper.getRandom;

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
    //@Transient
    //private Queue<Tile> currentPath;
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
            List<Tile> neighbours = getTile().getNeighbours()
                    .stream().filter(tile -> this.getNaturalHabitat().movavableSurfaces.contains(
                            tile.getSurfaceType()))
                    .collect(Collectors.toList());
            Optional<Float> bestForagingValue = neighbours.stream().max(Comparator.comparing(tile -> tile.getBiome().getCurrentFood())).map(Tile::getBiome).map(Biome::getCurrentFood);
            if(!bestForagingValue.isPresent()){
                moveToTile(neighbours.get(getRandom(neighbours.size())));
            } else {
                neighbours = neighbours.stream()
                        .filter(tile -> tile.getBiome().getCurrentFood() >= bestForagingValue.get())
                        .collect(Collectors.toList());
                int random = getRandom(neighbours.size());
                moveToTile(neighbours.get(random));
            }

            /*
            // advanced logic
            if(currentPath.isEmpty()) {
                Queue<Tile> tileQueue = new LinkedList<>();
                tileQueue.add(getTile());
                Queue<Tile> pathToNearestFoodsource = nearestFoodTile(Collections.singletonList(tileQueue), new ArrayList<>(), 0L);
                super.setTile(pathToNearestFoodsource.peek());
                pathToNearestFoodsource.remove();
                if (!pathToNearestFoodsource.isEmpty()){
                    currentPath = pathToNearestFoodsource;
                }
            } else {
                super.setTile(currentPath.peek());
                currentPath.remove();
            }*/
        }
    }

    private void moveToTile(Tile tile) {
        getTile().getActors().remove(this);
        setTile(tile);
        getTile().getActors().add(this);
    }

/*
    private Queue<Tile> searchPathToNearestFood(List<LinkedList<Tile>> paths, List<Tile> exclude, Long itteration) {
        if(itteration == FORAGING_RANGE){
            Queue<Tile> noPathFound = new LinkedList<>();
            noPathFound.add(getTile());
            return noPathFound;
        }

        final List<Tile> nextTiles = new ArrayList<>();
        paths.forEach(
                path -> nextTiles.addAll(path.getLast().getNeighbours())
        );

        nextTiles.removeAll(exclude);
        if(getNaturalHabitat() != SurfaceType.ALL) {
            nextTiles.removeAll(nextTiles.stream().filter(
                    tile -> tile.getSurfaceType() != getNaturalHabitat()
                ).collect(Collectors.toList())
            );
        }

        List<Tile> possibleLocations = getTile().getNeighbours().stream()
            .filter(neigbouringTile -> neigbouringTile.getBiome().getCurrentFood() >= foraging)
            .collect(Collectors.toList());

        if(possibleLocations.isEmpty()){
            return searchPathToNearestFood(paths, nextTiles, itteration+1);
        } else if(possibleLocations.size() == 1){
            path.add(possibleLocations.get(0));
            return path;
        } else {
            Random random = new Random();
            path.add(possibleLocations.get(random.nextInt(nextTiles.size())));
            return path;
        }
    }
*/

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
