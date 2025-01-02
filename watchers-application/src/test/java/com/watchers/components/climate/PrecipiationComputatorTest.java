package com.watchers.components.climate;

import com.watchers.TestableWorld;
import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrecipiationComputatorTest {
    private PrecipiationComputator computator;
    private World world;
    private WorldTaskDto taskDto;

    @BeforeEach
    void setUp() {
        // Creëer de wereld met de TestableWorld.createWorld methode
        world = TestableWorld.createWorld();  // Gebruik de gegenereerde wereld
        taskDto = new WorldTaskDto(world.getWorldMetaData());  // Maak de taskDto voor de test
        taskDto.setWorld(world);
        computator = new PrecipiationComputator();  // Instantieer de PrecipiationComputator
    }

    // Testen voor de methoden volgen hieronder
    @Test
    void testComputeEvaporation() {
        // Verkrijg de lijst van klimaten van de coördinaten
        List<Climate> climates = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());

        climates.forEach(climate -> climate.setMaximalAirMoisture(100));

        // Roep de methode aan om verdamping te verwerken
        computator.computeEvaporation(climates);

        // Controleer of alle waterklimaten een luchtvochtigheid van 10 hebben
        climates.stream()
                .filter(Climate::isWater)
                .forEach(climate -> assertEquals(10, climate.getAirMoisture(),
                        "De luchtvochtigheid van waterklimaten moet 10 zijn"));

        // Controleer of alle landklimaten een luchtvochtigheid van 0 hebben
        climates.stream()
                .filter(Climate::isLand)
                .forEach(climate -> assertEquals(0, climate.getAirMoisture(),
                        "De luchtvochtigheid van landklimaten moet 0 zijn"));
    }

    @Test
    void testMoveCloudsAccordingToAirflow() {
        WorldSettings worldSettings = world.getWorldSettings();

        List<Climate> climates = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());

        // Stel in dat er uitgaande luchtstromen zijn (we gaan ervan uit dat je een luchtstroom hebt in je wereldobjecten)
        climates.stream()
                .flatMap(climate -> climate.getOutgoingAircurrents().stream())
                .forEach(aircurrent -> aircurrent.setCurrentStrength(0));

        // Roep de methode aan om de wolken te verplaatsen volgens de luchtstromen
        computator.moveCloudsAccordingToAirflow(climates, worldSettings);

        // Controleer of de luchtstroomsterkte correct is ingesteld
        for (Climate climate : climates) {
            assertEquals(worldSettings.getLongitudinalStrength(), climate.getOutgoingLongitudinalAirflow().getCurrentStrength(),
                    "The aircurrent strenght needs to be equal to the worldsettings for longitudal airflows");
            assertEquals(worldSettings.getLatitudinalStrength(), climate.getOutgoingLatitudinalAirflow().getCurrentStrength(),
                    "The aircurrent strenght needs to be equal to the worldsettings for latitudal airflows");
        }
    }

    @Test
    void testProcessPrecipitation() {

        computator.process(taskDto);

        world.getCoordinates().forEach((Coordinate coordinate) -> {
            Climate climate = coordinate.getClimate();
            if(coordinate.isWater()) {
                assertEquals(0, coordinate.getTile().getRainfall());
            }

            double incomingMoisture = coordinate.getClimate().getIncomingAircurrents().stream()
                    .mapToDouble(aircurrent -> {
                        Climate startingClimate = aircurrent.getStartingClimate();
                        double climateMoisture = startingClimate.isWater()?10d:0d;
                        double totalStrength = startingClimate.getOutgoingAircurrents().stream()
                                .mapToDouble(Aircurrent::getCurrentStrength)
                                .sum();
                        return climateMoisture / totalStrength * aircurrent.getCurrentStrength();
                    }).sum();
            double maximalNightMoisture = climate.getMaximalAirMoistureNight();
            if(incomingMoisture > maximalNightMoisture && climate.isLand()) {
                double airMoisture = climate.getAirMoisture();
                assertEquals(maximalNightMoisture, airMoisture, 1d);
                double excessAirMoisture = incomingMoisture - maximalNightMoisture;
                double rainfall = coordinate.getTile().getRainfall();
                assertEquals(rainfall, excessAirMoisture,1d);
            }
        });

    }
}

