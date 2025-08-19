package com.s92077274.uninav.models;

import com.google.android.gms.maps.model.LatLng;

public class MapPoint {
    public String name;
    public String description;
    public float x; // Latitude
    public float y; // Longitude
    public String category;
    public boolean isUserLocation;

    // Primary constructor for a map point
    public MapPoint(String name, String description, float x, float y, String category) {
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
        this.category = category;
        this.isUserLocation = false;
    }

    // Constructor with default description and category
    public MapPoint(String name, float x, float y) {
        this(name, "", x, y, "general");
    }

    // Default constructor
    public MapPoint() {
    }

    // Converts MapPoint coordinates to a Google Maps LatLng object
    public LatLng toLatLng() {
        return new LatLng(x, y);
    }
}