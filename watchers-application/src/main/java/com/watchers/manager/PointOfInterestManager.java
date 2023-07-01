package com.watchers.manager;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.repository.CoordinateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PointOfInterestManager {

    private final CoordinateRepository coordinateRepository;


    @Transactional
    public void addHotspot(Long xCoord, Long yCoord) {
        Optional<Coordinate> optional = coordinateRepository.findByCoordinates(xCoord, yCoord);
        optional.ifPresent(HotSpotCrystal::new);
    }

    @Transactional
    public void removeHotspot(Long xCoord, Long yCoord) {
        Optional<Coordinate> optional = coordinateRepository.findByCoordinates(xCoord, yCoord);
        optional.map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof HotSpotCrystal)
                .ifPresent(hotspotCrystal -> hotspotCrystal.setCoordinate(null));
    }

    @Transactional
    public void addTectonicPlume(Long xCoord, Long yCoord) {
        Optional<Coordinate> optional = coordinateRepository.findByCoordinates(xCoord, yCoord);
        optional.ifPresent(TectonicCrystal::new);
    }

    @Transactional
    public void removeTectonicPlume(Long xCoord, Long yCoord) {
        Optional<Coordinate> optional = coordinateRepository.findByCoordinates(xCoord, yCoord);
        optional.map(Coordinate::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof TectonicCrystal)
                .ifPresent(hotspotCrystal -> hotspotCrystal.setCoordinate(null));
    }
}
