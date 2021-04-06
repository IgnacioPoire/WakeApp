package com.wakeapp.models.Alarm;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.sql.Time;

public class Alarm implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isEnabled;
    private boolean alwaysActive;
    private String name;
    private double lat;
    private double lng;
    private double radius;
    private Time time;
    private long interval;
    private long endTime;

    public Alarm(String name, LatLng latLng, double radius) {
        this.isEnabled = true;
        this.alwaysActive = true;
        this.name = name;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.radius = radius;
        this.time = new Time(43200000);
        this.endTime = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public void setLatLng(LatLng latLng) {
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double value) {
        this.radius = value;
    }

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
    }

    public boolean getAlwaysActive() {
        return this.alwaysActive;
    }

    public void setAlwaysActive(boolean value) {
        this.alwaysActive = value;
    }

    public Time getTime() {
        return this.time;
    }

    public void setTime(long value) {
        this.time = new Time(value);
    }

    public Time getInterval() {
        return this.time;
    }

    public void setInterval(long value) {
        this.endTime = this.time.getTime() + interval;
    }
}
