package com.s92077274.uninav.models;

import java.util.List;

public class NavigationRoute {
    public List<MapPoint> waypoints;
    public List<String> instructions;
    public String totalDistance;
    public String estimatedTime;

    // Constructor to initialize navigation route
    public NavigationRoute(List<MapPoint> waypoints, List<String> instructions) {
        this.waypoints = waypoints;
        this.instructions = instructions;
        this.totalDistance = calculateDistance();
        this.estimatedTime = calculateTime();
    }

    // Calculates the total distance of the route
    private String calculateDistance() {
        return "150m";
    }

    // Calculates the estimated time for the route
    private String calculateTime() {
        return "2 min";
    }
}