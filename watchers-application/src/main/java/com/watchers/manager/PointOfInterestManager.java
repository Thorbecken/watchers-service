package com.watchers.manager;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.model.world.World;
import com.watchers.repository.CoordinateRepository;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PointOfInterestManager {

    private final WorldRepository worldRepository;

    @Transactional
    public void addHotspot(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new HotSpotCrystal(coordinate);
        worldRepository.save(world);
    }

    @Transactional
    public void removeHotspot(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        if (coordinate.getPointOfInterest() != null
                && coordinate.getPointOfInterest() instanceof HotSpotCrystal) {
            coordinate.getPointOfInterest().setCoordinate(null);
        }
    }

    @Transactional
    public void addTectonicPlume(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        new TectonicCrystal(coordinate);
        worldRepository.save(world);
    }

    @Transactional
    public void removeTectonicPlume(Long xCoord, Long yCoord) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate coordinate = world.getCoordinate(xCoord, yCoord);
        if (coordinate.getPointOfInterest() != null
                && coordinate.getPointOfInterest() instanceof TectonicCrystal) {
            coordinate.getPointOfInterest().setCoordinate(null);
        }
    }
}
