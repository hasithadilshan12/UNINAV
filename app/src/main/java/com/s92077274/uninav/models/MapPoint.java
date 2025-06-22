package com.s92077274.uninav.models;

public class MapPoint {
    public String name;
    public String description;
    public float x;
    public float y;
    public String category;
    public boolean isUserLocation;

    public MapPoint(String name, String description, float x, float y, String category) {
        this.name = name;
        this.description = description;
        this.x = x;
        this.y = y;
        this.category = category;
        this.isUserLocation = false;
    }

    public MapPoint(String name, float x, float y) {
        this(name, "", x, y, "general");
    }

    public MapPoint() {

    }
}

