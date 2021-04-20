package com.wakeapp.models.Alarm;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Alarm implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isEnabled;
    private boolean timeActive;
    private boolean daysActive;
    private String name;
    private double lat;
    private double lng;
    private double radius;
    private Time time;
    private long interval;
    private long endTime;
    private ArrayList<Boolean> days;

    public Alarm(String name, LatLng latLng, double radius) {
        this.isEnabled = true;
        this.timeActive = true;
        this.daysActive = true;
        this.name = name;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.radius = radius;
        this.time = new Time(43200000);
        this.interval = 0;
        this.endTime = 0;
        this.days = new ArrayList<>(Arrays.asList(new Boolean[7]));
        Collections.fill(this.days, Boolean.TRUE);
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

    public boolean getTimeActive() {
        return this.timeActive;
    }

    public void setTimeActive(boolean value) {
        this.timeActive = value;
    }

    public boolean getDaysActive() {
        return this.daysActive;
    }

    public void setDaysActive(boolean value) {
        this.daysActive = value;
    }

    public Time getTime() {
        return this.time;
    }

    public void setTime(long value) {
        this.time = new Time(value);
    }

    public long getInterval() {
        return this.interval;
    }

    public void setInterval(long value) {
        this.interval = value;
        if (this.time.getTime() + this.interval >= 86400000L) {
            this.endTime = this.time.getTime() + this.interval - 86400000L;
        } else {
            this.endTime = this.time.getTime() + this.interval;
        }
    }

    public long getEndtime() { return this.endTime; }

    public void setDays(int id, boolean value) {
        this.days.set(id, value);
    }

    public ArrayList<Boolean> getDays() {
        return this.days;
    }
}
