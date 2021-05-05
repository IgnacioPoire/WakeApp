package com.wakeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import com.wakeapp.models.alarms.Alarm;
import com.wakeapp.models.alarms.GeoAlarm;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements VariableInterface, OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2;
    private AppBarConfiguration mAppBarConfiguration;
    private ArrayList<GeoAlarm> geoAlarms;
    private ArrayList<Alarm> alarms;
    private NavController navController;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadAlarms();
        setContentView(R.layout.activity_main);
        checkPermission();
        startAlarmListener();
    }

    private void permissionsGranted() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_maps, R.id.nav_alarms, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    1);
        } else {
            permissionsGranted();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if permissions have been granted, do the following
                    permissionsGranted();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if permissions have been granted, do the following
                    permissionsGranted();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if permissions have been granted, do the following
                    permissionsGranted();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemSettings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuItemHelp:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public ArrayList<GeoAlarm> getGeoAlarmList() {
        return geoAlarms;
    }

    @Override
    public ArrayList<Alarm> getAlarmList() {
        return alarms;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            checkFileExists("/geoalarms.txt");
            File alarmsFile = new File(getExternalFilesDir(null) + "/geoalarms.txt");
            FileInputStream fin = new FileInputStream(alarmsFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                geoAlarms = (ArrayList<GeoAlarm>) is.readObject();
                is.close();
            }
            fin.close();
            System.out.print("LOADED " + geoAlarms);
            checkFileExists("/alarms.txt");
            alarmsFile = new File(getExternalFilesDir(null) + "/alarms.txt");
            fin = new FileInputStream(alarmsFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                alarms = (ArrayList<Alarm>) is.readObject();
                is.close();
            }
            fin.close();
            System.out.print("LOADED " + alarms);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkFileExists(String filename) {
        File alarmFile = new File(getExternalFilesDir(null) + filename);
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

    private void loadAlarms() {
        //LOAD FROM DB
        try {
            checkFileExists("/geoalarms.txt");
            File alarmsFile = new File(getExternalFilesDir(null) + "/geoalarms.txt");
            FileInputStream fin = new FileInputStream(alarmsFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                geoAlarms = (ArrayList<GeoAlarm>) is.readObject();
                is.close();
            } else {
                geoAlarms = new ArrayList<GeoAlarm>();
            }
            fin.close();
            System.out.print("LOADED " + geoAlarms);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            checkFileExists("/alarms.txt");
            File alarmsFile = new File(getExternalFilesDir(null) + "/alarms.txt");
            FileInputStream fin = new FileInputStream(alarmsFile);
            if (fin.available() != 0) {
                ObjectInputStream is = new ObjectInputStream(fin);
                alarms = (ArrayList<Alarm>) is.readObject();
                is.close();
            } else {
                alarms = new ArrayList<Alarm>();
            }
            fin.close();
            System.out.print("LOADED " + alarms);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startAlarmListener() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AlarmListener.class.getName().equals(service.service.getClassName())) {
                System.out.println("Listener running");
                return;
            }
        }
        Intent listener = new Intent(this, AlarmListener.class);
        System.out.println("Listener: " + listener);
        startService(listener);
        System.out.println("Listener started");
    }
}