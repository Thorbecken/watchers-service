package com.watchers.model.climate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloudTest {

    @Test
    void addAirMoistureAndReduceAirMoisture() {
        Cloud cloud = new Cloud();
        assertEquals(0, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(50, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(100, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(100, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(50, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(0, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(0, cloud.getAirMoisture());
    }
}