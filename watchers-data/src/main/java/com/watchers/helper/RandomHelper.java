package com.watchers.helper;

import com.watchers.model.environment.Tile;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomHelper {
    private static final Random random = new Random();

    public static int getRandom(int highestValue) {
        return random.nextInt(highestValue);
    }

    public static long getRandomLong(long highestValue) {
        int upperbound = (int) highestValue+1;
        return random.nextInt(upperbound);
    }

    public static long getRandomNonZero(long highestValue) {
        if (highestValue > 1L) {
            return random.nextInt(((int) highestValue)) + 1;
        } else if (highestValue == 1L) {
            return 1L;
        } else {
            return 0;
        }
    }

    public static int getRandomWithNegativeNumbers(int max) {
        int range = max * 2;
        int randomNumber = random.nextInt(range);
        return randomNumber - max;
    }

    public static Tile getRandomHighestTile(List<Tile> tiles) {
        tiles.sort(Comparator.comparing(Tile::getHeight));
        long maxHeight = tiles.get(tiles.size() - 1).getHeight();
        List<Tile> maxTiles = tiles.stream()
                .filter(tile -> tile.getHeight() == maxHeight)
                .collect(Collectors.toList());
        return maxTiles.get(RandomHelper.getRandom(maxTiles.size()));
    }
}
