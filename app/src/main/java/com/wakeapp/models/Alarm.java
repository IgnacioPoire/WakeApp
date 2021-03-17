package com.wakeapp.models;

import com.google.android.gms.maps.model.LatLng;

public class Alarm {
    private boolean isEnabled;
    private String name;
    private LatLng latLng;
    private double radius;

    public Alarm(String name, LatLng latLng, double radius) {
        this.isEnabled = true;
        this.name = name;
        this.latLng = latLng;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setEnabled() {
        this.isEnabled = true;
    }

    public void setDisabled() {
        this.isEnabled = false;
    }
}
