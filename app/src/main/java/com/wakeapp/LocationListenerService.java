package com.wakeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.wakeapp.models.alarms.GeoAlarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class LocationListenerService extends Service {

    //STATICS
    private static final String CLASS_NAME = "LocationListenerService";
    private static final String CHANNEL_ID = "LOCATION_NOTIFICATION_CHANNEL";
    private static final int LOCATION_SERVICE_ID = 175;
    private static final int LOCATION_REFRESH_TIME = 1500;
    private static final int LOCATION_FASTEST_REFRESH_TIME = 500;

    //BINDER TO ACTIVITY
    private final IBinder mBinder = new LocationListenerServiceBinder();

    //SAVED GEO-ALARMS
    private ArrayList<GeoAlarm> activeGeoAlarms;

    //LOCATION VARIABLES
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location userLocation;
    private boolean tracking = false;
    private LocationRequest locationRequest = LocationRequest.create()
            .setInterval(LOCATION_REFRESH_TIME)
            .setFastestInterval(LOCATION_FASTEST_REFRESH_TIME)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            userLocation = locationResult.getLastLocation();
            Log.d(CLASS_NAME, "LOCATION_UPDATE: " + userLocation.toString());
        }
    };

    //ALARM SYSTEM
    private BroadcastReceiver tickReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                checkGeoAlarms();
            }
        }
    };

    //BINDER TO ACTIVITY
    public class LocationListenerServiceBinder extends Binder {
        LocationListenerService getBinder() {
            return LocationListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //SERVICE
    @Override
    public void onCreate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }
        super.onCreate();
        loadGeoAlarms();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        checkSettingsAndStartLocationUpdates();
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    //SERVICE STOPS
    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "STOPPED LOCATION UPDATES");
        super.onDestroy();
    }

    //ALARM FUNCTIONS
    private void checkGeoAlarms() {
        Calendar now = Calendar.getInstance();

        if (tracking) {
            for (GeoAlarm alarm : activeGeoAlarms) {
                if (!wasTriggered(now, alarm)) {
                    if (alarm.getDaysActive()) {
                        if (alarm.getTimeActive()) {
                            if (checkGeoAlarm(alarm)) {
                                return;
                            }
                        } else {
                            if (timeCheck(now, alarm)) {
                                if(checkGeoAlarm(alarm)){
                                    return;
                                }
                            }
                        }
                    } else {
                        if (alarm.getTimeActive()) {
                            if (checkDayOfWeek(now.get(Calendar.DAY_OF_WEEK), alarm)) {
                                if(checkGeoAlarm(alarm)){
                                    return;
                                }
                            }
                        } else {
                            if (timeCheck(now, alarm)
                                    && checkDayOfWeek(now.get(Calendar.DAY_OF_WEEK), alarm)) {
                                if(checkGeoAlarm(alarm)){
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean wasTriggered(Calendar now, GeoAlarm alarm) {
        if (alarm.getLastTrigger() != null) {
            long timeInMillis = now.getTimeInMillis();
            long lastTriggerInMillis = alarm.getLastTrigger().getTimeInMillis();

            return (lastTriggerInMillis < timeInMillis + alarm.getSleep() * 15 * 1000);
        } else {
            return false;
        }
    }

    private boolean checkDayOfWeek(int dayOfWeekNow, GeoAlarm alarm) {
        return alarm.getDays().get(dayOfWeekNow - 1);
    }

    private boolean timeCheck(Calendar now, GeoAlarm alarm) {
        long start = alarm.getHour() * 60 + alarm.getMinutes();
        long eval = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        long end = alarm.getEndHour() * 60 + alarm.getEndMinutes();
        long endInterval = start + alarm.getInterval() * 30;

        if (start < end) {
            return start <= eval && eval <= end;
        } else {
            if (eval < start) {
                eval = eval + 1440;
            }
            return start <= eval && eval <= endInterval;
        }
    }

    private boolean checkGeoAlarm(GeoAlarm alarm) {
        if (userLocation != null) {
            double distanceUserAndAlarm = haversine(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    alarm.getLatLng().latitude,
                    alarm.getLatLng().longitude
            );

            if (distanceUserAndAlarm <= alarm.getRadius()) {
                return triggerAlarm(alarm);
            }
        }

        return false;
    }

    double haversine(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private boolean triggerAlarm(GeoAlarm alarm) {
        Log.d("ALARM TRIGGER", "Alarm was triggered");
        alarm.setLastTrigger(Calendar.getInstance());
        return true;
    }

    //CHECK
    private void checkSettingsAndStartLocationUpdates() {

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(locationSettingsRequest);

        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(CLASS_NAME, "STARTED LOCATION UPDATES");
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    Log.d(CLASS_NAME, "FAIL TO START LOCATION UPDATES :" + apiException.getMessage());
                    tracking = false;
                }
            }
        });
    }

    //START LOCATION UPDATES
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                CHANNEL_ID
        );

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(LOCATION_SERVICE_ID, builder.build());
        tracking = true;
    }

    //STOP LOCATION UPDATES
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopLocationUpdates() {
        unregisterReceiver(tickReceiver);
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        tracking = false;
        stopSelf();
    }

    //FIRST GET LOCATION TRY
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
         Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
         locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
             @Override
             public void onSuccess(Location location) {
                 System.out.println("getLastLocation");
                 if (location != null){
                     System.out.println("Location Updated");
                     userLocation = location;
                 } else {
                     System.out.println("Location is NULL");
                 }
             }
         });
    }

    //LOADS ALL GEO-ALARMS FROM FILE
    public void loadGeoAlarms() {
        try {
            checkFileExists();
            File alarmFile = new File(getExternalFilesDir(null) + "/geoalarms.txt");
            FileInputStream fin = new FileInputStream(alarmFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                ArrayList<GeoAlarm> geoAlarms = (ArrayList<GeoAlarm>) is.readObject();
                activeGeoAlarms = getOnlyEnabled(geoAlarms);
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

    private ArrayList<GeoAlarm> getOnlyEnabled(ArrayList<GeoAlarm> geoAlarms) {
        ArrayList<GeoAlarm> activeGeoAlarms = new ArrayList<>();
        for (GeoAlarm geoAlarm : geoAlarms ) {
            if (geoAlarm.getIsEnabled()) {
                activeGeoAlarms.add(geoAlarm);
            }
        }

        return activeGeoAlarms;
    }

    //CHECK IF FILE EXISTS - IF NOT THEN CREATE IT
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopService() {
        this.stopLocationUpdates();
    }
}