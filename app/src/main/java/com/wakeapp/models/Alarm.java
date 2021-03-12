package com.wakeapp.models;

import com.google.android.gms.maps.model.LatLng;

public class Alarm {
    private String locationName;
    private LatLng latLng;
    private double radius;

    public Alarm(String locationName, LatLng latLng, double radius) {
        this.locationName = locationName;
        this.latLng = latLng;
        this.radius = radius;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
