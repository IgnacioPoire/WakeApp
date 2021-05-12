package com.wakeapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.wakeapp.models.alarms.GeoAlarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class AlarmListener extends Service implements LocationListener {
    private ArrayList<GeoAlarm> activeGeoAlarms;

    private boolean isGPSEnable = false;
    private boolean isNetworkEnable = false;
    private double latitude, longitude;
    private LocationManager locationManager;
    private Location userLocation;
    private double distanceBetweenUserAlarm;
    private final long LOCATION_REFRESH_TIME = 1000;
    private final float LOCATION_REFRESH_DISTANCE = 20;
    private final IBinder mBinder = new AlarmListenerBinder();
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            System.out.println("Location Change");
            if (location != null) {
                System.out.println("Location Updated");
                userLocation = location;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            if (userLocation != null) {
                for (int i = 0; i < activeGeoAlarms.size(); i++) {
                    distanceBetweenUserAlarm = haversine(userLocation.getLatitude(), userLocation.getLongitude(), activeGeoAlarms.get(i).getLatitude(), activeGeoAlarms.get(i).getLongitude());

                    if (distanceBetweenUserAlarm <= activeGeoAlarms.get(i).getRadius()) {
                        System.out.println("USER IS INSIDE THE RADIOUS");
                    }
                }
            }
        }
    };

    public AlarmListener() {
        System.out.println("[+] CREATED A NEW ALARMLISTENER INSTANCE, ALREADY IN AlarmListener()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadGeoAlarms();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(bestProvider, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);
        /*if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, locationListener);
        }*/

        System.out.println("[+] CREATED A NEW ALARMLISTENER INSTANCE, ALREADY IN onCreate");
    }

    public class AlarmListenerBinder extends Binder {
        AlarmListener getBinder() {
            return AlarmListener.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocation = location;
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    /**
     * Returns distance in meters between two points
     */
    double haversine(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

//    private void createNotification(){
//        Intent i = new Intent(this, MainActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                .setContentTitle("I want food")
//                .setContentText(notificationcontent)
//                .setSmallIcon(R.drawable.ic_launcher)
//                .setContentIntent(pi)
//                .setAutoCancel(true)
//                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        MediaPlayer mp= MediaPlayer.create(contexto, R.raw.your_sound);
//        mp.start();
//        manager.notify(73195, builder.build());
//    }

    public void loadGeoAlarms() {
        try {
            checkFileExists();
            File alarmFile = new File(getExternalFilesDir(null) + "/geoalarms.txt");
            FileInputStream fin = new FileInputStream(alarmFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                this.activeGeoAlarms = (ArrayList<GeoAlarm>) is.readObject();
                is.close();
            } else {
                this.activeGeoAlarms = new ArrayList<>();
            }
            fin.close();
            System.out.print("LOADED " + this.activeGeoAlarms);
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
            if (!alarmFile.exists()) {
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

    public Location getUserLocation() {
        return userLocation;
    }
}