package com.watchers.model.environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiomeTest {

    private Biome biome;

    @BeforeEach
    void biomeSetup() {
        biome = new Biome(0f,10f,1f, null);
    }

    @Test
    void processParallelTask() {
        biome.processParallelTask();
        assertEquals(1f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(2f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(3f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(4f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(5f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(6f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(7f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(8f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(9f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(10f, biome.getCurrentFood());
        biome.processParallelTask();
        assertEquals(10f, biome.getCurrentFood());
        biome.setCurrentFood(9.9f);
        biome.processParallelTask();
        assertEquals(10f, biome.getCurrentFood());
    }
}