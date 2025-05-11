package com.kitchenplus.kitchenplus.utils;

public class DistanceUtils {
    private double latitudeWareHouse = 54.89077;
    private double longitudeWareHouse = 23.919353;
    private double radiusOfEarth = 6371;

    public double distanceHervesine(String lat2, String lon2) {
        double lat2_ = Double.parseDouble(lat2);
        double lon2_ = Double.parseDouble(lon2);
        double dLat = Math.toRadians(lat2_ - latitudeWareHouse);
        double dLon = Math.toRadians(lon2_ - longitudeWareHouse);
        latitudeWareHouse = Math.toRadians(latitudeWareHouse);
        lat2_ = Math.toRadians(lat2_);
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(latitudeWareHouse) *
                        Math.cos(lat2_);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radiusOfEarth * c;
    }
}
