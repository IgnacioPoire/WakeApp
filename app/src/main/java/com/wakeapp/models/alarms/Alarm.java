package com.wakeapp.models.alarms;

import java.io.Serializable;
import java.util.ArrayList;

public class Alarm implements Serializable {
    private static final long serialVersionUID = 2L;

    private boolean isEnabled;
    private boolean daysActive;
    private String name;
    private int timeHour;
    private int timeMinutes;
    private ArrayList<Boolean> days;

    public Alarm(String name, boolean active, int hour, int minutes, ArrayList<Boolean> days) {
        this.isEnabled = true;
        this.daysActive = active;
        this.name = name;
        this.timeHour = hour;
        this.timeMinutes = minutes;
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean value) {
        this.isEnabled = value;
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

    public void setDays(ArrayList<Boolean> values) {
        this.days = values;
    }

    public ArrayList<Boolean> getDays() {
        return this.days;
    }
}
