package com.watchers.manager;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Flora;
import com.watchers.model.special.base.PointOfInterest;
import com.watchers.model.special.crystal.AquiferCrystal;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.model.special.life.GreatFlora;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PointOfInterestManager {

    private final WorldRepository worldRepository;

    private final int numberOfTurnsBeforeReallocationOfMantlePlume;
    private final int numberOfTurnsBeforeReallocationOfVolcano;

    public PointOfInterestManager(WorldRepository worldRepository,
                                  @Value("${watch.continent.mantle-plume.turn-limit}") int numberOfTurnsBeforeReallocationOfMantlePlume,
                                  @Value("${watch.continent.volcano.turn-limit}") int numberOfTurnsBeforeReallocationOfVolcano){
        this.worldRepository = worldRepository;
        this.numberOfTurnsBeforeReallocationOfMantlePlume = numberOfTurnsBeforeReallocationOfMantlePlume;
        this.numberOfTurnsBeforeReallocationOfVolcano = numberOfTurnsBeforeReallocationOfVolcano;
    }

    @Transactional
    public void addHotspot(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new HotSpotCrystal(coordinate, numberOfTurnsBeforeReallocationOfVolcano);
        worldRepository.save(world);
    }

    @Transactional
    public void addTectonicPlume(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new TectonicCrystal(coordinate, numberOfTurnsBeforeReallocationOfMantlePlume);
        worldRepository.save(world);
    }

    @Transactional
    public void addAquifer(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new AquiferCrystal(coordinate.getTile());
        worldRepository.save(world);
    }

    public void plantGreatFlora(Long xCoord, Long yCoord, Flora flora) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new GreatFlora(coordinate.getTile(), flora);
        worldRepository.save(world);
    }

    @Transactional
    public String removePointOfInterest(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        PointOfInterest pointOfInterest = coordinate.getPointOfInterest() != null ? coordinate.getPointOfInterest() : coordinate.getTile().getPointOfInterest();
        if(pointOfInterest != null){
            pointOfInterest.setCoordinate(null);
            pointOfInterest.setTile(null);
            if(pointOfInterest instanceof HotSpotCrystal) {
                world.setHeightDeficit(world.getHeightDeficit() + ((HotSpotCrystal) pointOfInterest).getHeightBuildup());
            }
            worldRepository.save(world);
            return pointOfInterest.getClass().getSimpleName();
        } else {
            return "non existing point of interest";
        }
    }
}
