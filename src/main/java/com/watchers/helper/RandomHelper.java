package com.watchers.helper;

import java.util.Random;

public class RandomHelper {
    private static Random random = new Random();

    public static int getRandom(int highestValue){
        return random.nextInt(highestValue);
    }
}
