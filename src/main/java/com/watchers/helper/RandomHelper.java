package com.watchers.helper;

import com.watchers.model.environment.Tile;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomHelper {
    private static Random random = new Random();

    public static int getRandom(int highestValue){
        return random.nextInt(highestValue);
    }

    public static int getRandomWithNegativeNumbers(int max){
        int range = max*2;
        int randomNumber = random.nextInt(range);
        return randomNumber - max;
    }

    public static Tile getRandomHighestTile(List<Tile> tiles) {
        long maxHeight = tiles.get(0).getHeight();
        List<Tile> maxTiles =  tiles.stream()
                .filter(tile -> tile.getHeight() == maxHeight)
                .collect(Collectors.toList());
        return maxTiles.get(RandomHelper.getRandom(maxTiles.size()));
    }
}
