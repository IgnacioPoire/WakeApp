package com.wakeapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.wakeapp.models.Alarm.Alarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmListener extends Service implements LocationListener {
    private ArrayList<Alarm> activeAlarms;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;


    public AlarmListener() {
        loadAlarms();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
//        fn_getlocation();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


    public void addActiveAlarm(Alarm alarm) {
        activeAlarms.add(alarm);
    }

    public void removeActiveAlarm(Alarm alarm) {
        int i = activeAlarms.indexOf(alarm);
        activeAlarms.remove(i);
    }

    public void waitForInteraction() {
        /*
         * NAME: waitForInteraction()
         * INPUT: -
         * BEHAVIOR: Constantly calculates the distance between the alarm radius and the actual user position. When the distance is less or equal than the radius, a new push notification will ring!
         */

        for (int i = 0; i < activeAlarms.size(); i++) {
            LatLng alarmLatLng = activeAlarms.get(i).getLatLng();
            //Location userLatLng = getCurrentUserLocation();
        }

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location!=null){

                        System.out.println("LATITUDE:"+location.getLatitude()+"");
                        System.out.println("Longitude:"+location.getLongitude()+"");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }

            }


            if (isGPSEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        System.out.println("LATITUDE:"+location.getLatitude()+"");
                        System.out.println("Longitude:"+location.getLongitude()+"");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }
            }


        }

    }

    private void fn_update(Location location){

        //intent.putExtra("latutide",location.getLatitude()+"");
        //intent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(intent);
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void getCurrentUserLocation() {

    }

    /**
     * Returns distance between locations in meters
     */
    double haversine(Location location1, Location location2) {
        return haversine(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());
    }

    /**
     * Returns distance in meters between two points
     */
    double haversine(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void loadAlarms() {
        try {
            checkFileExists();
            File alarmFile = new File(getExternalFilesDir(null) + "/alarms.txt");
            FileInputStream fin = new FileInputStream(alarmFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                this.activeAlarms = (ArrayList<Alarm>) is.readObject();
                is.close();
            } else {
                this.activeAlarms = new ArrayList<Alarm>();
            }
            fin.close();
            System.out.print("LOADED " + this.activeAlarms);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkFileExists() {
        File alarmFile = new File(getExternalFilesDir(null) + "/alarms.txt");
        try {
            if(!alarmFile.exists()) {
                alarmFile.getParentFile().mkdirs();
                alarmFile.createNewFile();
                FileOutputStream oFile = new FileOutputStream(alarmFile, true);
                oFile.close();
            }
        } catch (IOException e) {
            System.out.println("IOException in checkFileExists");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}