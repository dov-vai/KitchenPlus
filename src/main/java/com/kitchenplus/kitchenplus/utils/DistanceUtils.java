package com.kitchenplus.kitchenplus.utils;

import java.util.Arrays;

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
        double distance = radiusOfEarth * c;
        return distance;
    }
    private int[] setVertices(int V, int src){
        int[] dist = new int[V];
        Arrays.fill(dist, (int)1e8);
        dist[src] = 0;
        return dist;
    }
    public int[] BellmanFord(int V, int[][] edges, int src) {
        int[] dist = setVertices(V, src);

        // Relaxation of all the edges V times, not (V - 1) as we
        // need one additional relaxation to detect negative cycle
        for (int i = 0; i < V; i++) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int wt = edge[2];
                if (dist[u] != 1e8 && dist[u] + wt < dist[v]) {
                    // neigiamas ciklas:
                    if (i == V - 1)
                        return new int[]{-1};

                    // atnaujint virsunes:
                    dist[v] = dist[u] + wt;
                }
            }
        }
        return dist;
    }
}
