package com.watchers.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClimateHelperTest {

    @Test
    void roundToTwoDigits() {
        double unrounded = 10.200001d;
        assertEquals(ClimateHelper.roundToTwoDigits(unrounded), 10.20);
    }

    @Test
    void transformToLatitude() {
        double first = ClimateHelper.transformToLatitude(6, 6);
        double second = ClimateHelper.transformToLatitude(5, 6);
        double third = ClimateHelper.transformToLatitude(4, 6);
        double fourth = ClimateHelper.transformToLatitude(3, 6);
        double fifth = ClimateHelper.transformToLatitude(2, 6);
        double sixth = ClimateHelper.transformToLatitude(1, 6);

        assertEquals(90, first);
        assertEquals(60, second);
        assertEquals(30, third);
        assertEquals(0, fourth);
        assertEquals(-30, fifth);
        assertEquals(-60, sixth);
    }

    @Test
    void transformToLongitude() {
        double first = ClimateHelper.transformToLongitude(1, 4);
        double second = ClimateHelper.transformToLongitude(2, 4);
        double third = ClimateHelper.transformToLongitude(3, 4);
        double fourth = ClimateHelper.transformToLongitude(4, 4);

        assertEquals(90, first);
        assertEquals(180, second);
        assertEquals(270, third);
        assertEquals(360, fourth);

    }
}