package com.watchers.helper;

public class ClimateHelper {
    static public double roundToTwoDigits(double number){
        int temp = (int)(number*100);
        return temp/100d;
    }

    static public double transformToLatitude(double y, double ySize) {
        double rawLatitude = ((y / ySize) * 180d)-90d;
        return roundToTwoDigits(rawLatitude);
    }

    public static double transformToLongitude(double x, double wx) {
        double rawLongitude = x / wx * 360d;
        return roundToTwoDigits(rawLongitude);
    }
}
