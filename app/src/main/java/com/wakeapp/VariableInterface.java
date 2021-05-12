package com.wakeapp;

import android.location.Location;

import com.wakeapp.models.alarms.Alarm;
import com.wakeapp.models.alarms.GeoAlarm;

import java.util.ArrayList;

public interface VariableInterface {
    ArrayList<GeoAlarm> getGeoAlarmList();
    ArrayList<Alarm> getAlarmList();
    void updateListenerGeoAlarms();
    Location getUserLocation();
}
