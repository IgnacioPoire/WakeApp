package com.wakeapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

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
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        }*/
        super.onCreate();
        loadGeoAlarms();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        checkSettingsAndStartLocationUpdates();
    }

    //SERVICE STOPS
    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "STOPPED LOCATION UPDATES");
        super.onDestroy();
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
    }

    //STOP LOCATION UPDATES
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
    }

    public Location getUserLocation() {
        return userLocation;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopService() {
        this.stopLocationUpdates();
    }
}