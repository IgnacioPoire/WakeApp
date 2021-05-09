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
import com.wakeapp.models.alarms.GeoAlarm;

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
    private ArrayList<GeoAlarm> activeGeoAlarms;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location userLocation;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;

    double distanceBetweenUserAlarm;


    public AlarmListener() {
        //loadAlarms();
        System.out.println("[+] CREATED A NEW ALARMLISTENER INSTANCE, ALREADY IN AlarmListener()");
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        System.out.println("[+] CREATED A NEW ALARMLISTENER INSTANCE, ALREADY IN onStartCommand()");
//        return START_STICKY;
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadAlarms();
        System.out.println("[+] CREATED A NEW ALARMLISTENER INSTANCE, ALREADY IN onCreate");
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);
        intent = new Intent(str_receiver);
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


    public void addActiveAlarm(GeoAlarm geoAlarm) {
        activeGeoAlarms.add(geoAlarm);
    }

    public void removeActiveAlarm(GeoAlarm geoAlarm) {
        int i = activeGeoAlarms.indexOf(geoAlarm);
        activeGeoAlarms.remove(i);
    }

    public void waitForInteraction() {
        /*
         * NAME: waitForInteraction()
         * INPUT: -
         * BEHAVIOR: Constantly calculates the distance between the alarm radius and the actual user position. When the distance is less or equal than the radius, a new push notification will ring!
         */

        for (int i = 0; i < activeGeoAlarms.size(); i++) {
            LatLng alarmLatLng = activeGeoAlarms.get(i).getLatLng();
            //Location userLatLng = getCurrentUserLocation();
        }

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

            //ASK USER TO ENABLE BOTH OF THEM FOR RELIABILITY PURPOSES
            System.out.println("Neither GPS nor Network are enabled in the user's device");

        } else {

            if (isNetworkEnable) {
                userLocation = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (userLocation != null) {

                        System.out.println("LATITUDE by Network:" + userLocation.getLatitude() + "");
                        System.out.println("LONGITUDE by Network:" + userLocation.getLongitude() + "");

                        latitude = userLocation.getLatitude();
                        longitude = userLocation.getLongitude();
                        fn_update(userLocation);

                        for (int i = 0; i < activeGeoAlarms.size(); i++) {
                            distanceBetweenUserAlarm = haversine(userLocation.getLatitude(), userLocation.getLongitude(), activeGeoAlarms.get(i).getLatitude(), activeGeoAlarms.get(i).getLongitude());

                            if (distanceBetweenUserAlarm <= activeGeoAlarms.get(i).getRadius()) {
                                System.out.println("USER IS INSIDE THE RADIOUS");
                            }
                        }


                    }
                }

            }


            if (isGPSEnable) {
                userLocation = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (userLocation != null) {
                        latitude = userLocation.getLatitude();
                        longitude = userLocation.getLongitude();
                        System.out.println("LATITUDE by GPS:" + latitude + "");
                        System.out.println("LONGITUDE by GPS:" + longitude + "");
                        fn_update(userLocation);

                        for (int i = 0; i < activeGeoAlarms.size(); i++) {
                            distanceBetweenUserAlarm = haversine(userLocation.getLatitude(), userLocation.getLongitude(), activeGeoAlarms.get(i).getLatitude(), activeGeoAlarms.get(i).getLongitude());

                            if (distanceBetweenUserAlarm <= activeGeoAlarms.get(i).getRadius()) {
                                System.out.println("USER IS INSIDE THE RADIOUS");
                            }
                        }
                    }
                }
            }


        }

    }

    private void fn_update(Location location) {

        //intent.putExtra("latitude",location.getLatitude()+"");
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

    private void loadAlarms() {
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
}