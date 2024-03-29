package com.wakeapp.models.alarms;

import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public class GeoAlarm implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isEnabled;
    private boolean outsideActive;
    private boolean timeActive;
    private boolean daysActive;
    private String name;
    private double lat;
    private double lng;
    private double radius;
    private int timeHour;
    private int timeMinutes;
    private int interval;
    private int sleep;
    private int endTimeHour;
    private int endTimeMinutes;
    private Calendar lastTrigger;
    private ArrayList<Boolean> days;

    public GeoAlarm(String name, LatLng latLng, double radius) {
        this.isEnabled = true;
        this.outsideActive = false;
        this.timeActive = true;
        this.daysActive = true;
        this.name = name;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.radius = radius;
        this.timeHour = 12;
        this.timeMinutes = 0;
        this.interval = 0;
        this.sleep = 2;
        this.endTimeHour = 0;
        this.endTimeMinutes = 0;
        this.lastTrigger = null;
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

    public double getLatitude() {
        return this.lat;
    }

    public double getLongitude() { return this.lng; }

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

    public boolean getOutsideActive() {
        return this.outsideActive;
    }

    public void setOutsideActive(boolean value) {
        this.outsideActive = value;
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

    public int getHour() {
        return this.timeHour;
    }

    public void setHour(int value) { this.timeHour = value; }

    public int getMinutes() {
        return this.timeMinutes;
    }

    public void setMinutes(int value) { this.timeMinutes = value; }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int value) {
        this.interval = value;
        int hours = 0;
        int minutesSum = this.timeMinutes + interval * 30;
        while (minutesSum >= 60) {
            hours++;
            minutesSum = minutesSum - 60;
        }
        int hoursSum = this.timeHour + hours;
        this.endTimeHour = hoursSum >= 24 ? hoursSum - 24 : hoursSum;
        this.endTimeMinutes = minutesSum;
    }

    public int getSleep() {
        return this.sleep;
    }

    public void setSleep(int value) {
        this.sleep = value;
    }

    public int getEndHour() { return this.endTimeHour; }

    public int getEndMinutes() { return this.endTimeMinutes; }

    public Calendar getLastTrigger() { return this.lastTrigger; }

    public void setLastTrigger(Calendar value) { this.lastTrigger = value; }

    public void setDays(ArrayList<Boolean> values) {
        this.days = values;
    }

    public ArrayList<Boolean> getDays() {
        return this.days;
    }
}
