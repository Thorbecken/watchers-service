package com.watchers;

import com.watchers.config.SettingConfiguration;
import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.world.Continent;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSetting;
import com.watchers.model.world.WorldTypeEnum;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.*;

public class TestableWorld {

    public static World createWorld() {
        World world = new World(3, 3);
        world.setId(1L);
        world.setLastContinentInFlux(0L);

        WorldSetting worldSetting = new WorldSetting();
        worldSetting.setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(0L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(1, 1, world, continent1),
                CoordinateFactory.createCoordinate(1, 2, world, continent1),
                CoordinateFactory.createCoordinate(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(1L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(2, 2, world, continent2),
                CoordinateFactory.createCoordinate(3, 2, world, continent2),
                CoordinateFactory.createCoordinate(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(2L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(3, 3, world, continent3),
                CoordinateFactory.createCoordinate(3, 1, world, continent3),
                CoordinateFactory.createCoordinate(2, 3, world, continent3)
        ));

        world.setContinents(new HashSet<>(Arrays.asList(continent1, continent2, continent3)));
        world.setCoordinates(new HashSet<>());
        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());

        world.getCoordinates().forEach(coordinate -> {
                    coordinate.setWorld(coordinate.getWorld());
                    if (coordinate.getTile().getSurfaceType() == SurfaceType.PLAIN) {
                        coordinate.getTile().setHeight(40);
                    } else {
                        coordinate.getTile().setHeight(20);
                    }
                }
        );


        return world;
    }


    public static SettingConfiguration createConfiguration() {
        return new SettingConfiguration(
                getEnviormentMock(),

                false,
                1,
                10,
                30,
                60,
                "",

                12,
                6,
                3,
                true,
                2,
                5,

                1,
                1,
                2,
                2,
                6,
                4,
                9000,
                2,
                3,
                5,
                10
        );
    }

    private static Environment getEnviormentMock() {
        return new Environment() {
            @Override
            public String[] getActiveProfiles() {
                return new String[0];
            }

            @Override
            public String[] getDefaultProfiles() {
                return new String[0];
            }

            @Override
            @SuppressWarnings("all")
            public boolean acceptsProfiles(String... profiles) {
                return false;
            }

            @Override
            public boolean acceptsProfiles(Profiles profiles) {
                return false;
            }

            @Override
            public boolean containsProperty(String key) {
                return false;
            }

            @Override
            public String getProperty(String key) {
                return null;
            }

            @Override
            public String getProperty(String key, String defaultValue) {
                return null;
            }

            @Override
            public <T> T getProperty(String key, Class<T> targetType) {
                return null;
            }

            @Override
            public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
                return null;
            }

            @Override
            public String getRequiredProperty(String key) throws IllegalStateException {
                return null;
            }

            @Override
            public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
                return null;
            }

            @Override
            public String resolvePlaceholders(String text) {
                return null;
            }

            @Override
            public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
                return null;
            }
        };
    }
}
