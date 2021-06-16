package com.wakeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.CompoundButton;

import com.google.android.material.navigation.NavigationView;
import com.wakeapp.models.alarms.Alarm;
import com.wakeapp.models.alarms.GeoAlarm;
import com.wakeapp.services.LocationListenerService;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements VariableInterface, OnRequestPermissionsResultCallback {

    //STATICS
    private static final String CLASS_NAME = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS = 0;

    //MAIN ACTIVITY NAV
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    //SAVED ALARMS
    private ArrayList<GeoAlarm> geoAlarms;
    private ArrayList<Alarm> alarms;

    //LOCATION LISTENER SERVICE
    private LocationListenerService locationListenerService;
    @SuppressLint("ResourceType")
    private MenuItem trackingItem;
    private SwitchCompat trackingSwitch = null;
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            LocationListenerService.LocationListenerServiceBinder llsBinder =
                    (LocationListenerService.LocationListenerServiceBinder) binder;
            locationListenerService = llsBinder.getBinder();
            if (trackingSwitch != null) {
                trackingSwitch.setChecked(true);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            locationListenerService = null;
            if (trackingSwitch != null) {
                trackingSwitch.setChecked(true);
            }
        }
    };

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadTheme();
        loadLocalization();
        super.onCreate(savedInstanceState);
        if (checkMyPermissions()) {
            startLocationListenerService();
        }
        loadAllAlarms();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_maps, R.id.nav_alarms, R.id.nav_tracking)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        trackingMenuItemConfig(navigationView);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void loadLocalization() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String appLanguage = sp.getString("appLanguage", "en");
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void loadTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String appTheme = sp.getString("styleType", "dark");

        if ("dark".equals(appTheme)) {
            setTheme(getResources().getIdentifier("AppTheme",
                    "style",
                    getPackageName()));
        } else {
            appTheme = appTheme.substring(0, 1).toUpperCase() + appTheme.substring(1);
            setTheme(getResources().getIdentifier(appTheme,
                    "style",
                    getPackageName()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        super.onConfigurationChanged(newConfig);
    }

    private void trackingMenuItemConfig(NavigationView navigationView) {

        trackingItem = navigationView.getMenu().findItem(R.id.nav_tracking);
        trackingSwitch = (SwitchCompat) trackingItem.getActionView();

        if (locationListenerService != null) {
            trackingSwitch.setChecked(true);
        } else {
            trackingSwitch.setChecked(false);
        }

        trackingItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                trackingSwitch.performClick();
                return true;
            }
        });

        trackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkMyPermissions()) {
                        startLocationListenerService();
                    }
                } else {
                    locationListenerService.stopService();
                    unbindService(connection);
                    locationListenerService = null;
                }
            }
        });
    }

    //STARTUP PERMISSIONS CHECK
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean checkMyPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {

            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            } else if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            } else if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE);
            }

            requestPermissions(permissions.toArray(new String[0]),
                    MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }

    //ON PERMISSION RESULT
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ArrayList<String> permissionsArray = new ArrayList<>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsArray.add(permissions[i]);
            }
        }

        if(!permissionsArray.isEmpty()) {
            requestPermissions(permissionsArray.toArray(new String[0]),
                    MY_PERMISSIONS_REQUEST_ALL_PERMISSIONS);
        } else {
            startLocationListenerService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //OPTIONS MENU CASES
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

    //INTERFACE FUNCTIONS START
    @Override
    public ArrayList<GeoAlarm> getGeoAlarmList() {
        return geoAlarms;
    }

    @Override
    public ArrayList<Alarm> getAlarmList() {
        return alarms;
    }

    @Override
    public void updateListenerGeoAlarms() {
        if (locationListenerService != null) {
            locationListenerService.loadGeoAlarms();
        }
    }

    @Override
    public Location getUserLocation() {
        if (locationListenerService != null) {
            return locationListenerService.getUserLocation();
        }

        return null;
    }
    //INTERFACE FUNCTIONS END

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onResume() {
        loadAllAlarms();
        loadLocalization();
        loadTheme();
        super.onResume();
    }

    //LOADS ALL ALARMS FROM FILE
    private void loadAllAlarms() {
        try {
            loadGeoAlarms();
            loadAlarms();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //LOADS GEO-ALARMS FROM FILE
    private void loadGeoAlarms() throws IOException, ClassNotFoundException {
        checkFileExists("/geoalarms.txt");
        File alarmsFile = new File(getExternalFilesDir(null) + "/geoalarms.txt");
        FileInputStream fin = new FileInputStream(alarmsFile);
        if (fin.available() != 0) {
            ObjectInputStream is = new ObjectInputStream(fin);
            geoAlarms = (ArrayList<GeoAlarm>) is.readObject();
            is.close();
        } else {
            geoAlarms = new ArrayList<>();
        }
        fin.close();
        Log.d(CLASS_NAME, "GEO-ALARMS LOADED");
    }

    //LOADS ALARMS FROM FILE
    private void loadAlarms() throws IOException, ClassNotFoundException {
        checkFileExists("/alarms.txt");
        File alarmsFile = new File(getExternalFilesDir(null) + "/alarms.txt");
        FileInputStream fin = new FileInputStream(alarmsFile);
        if (fin.available() != 0) {
            ObjectInputStream is = new ObjectInputStream(fin);
            alarms = (ArrayList<Alarm>) is.readObject();
            is.close();
        } else {
            alarms = new ArrayList<>();
        }
        fin.close();
        Log.d(CLASS_NAME, "ALARMS LOADED");
    }

    //CHECK IF FILE EXISTS - IF NOT THEN CREATE IT
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
            e.printStackTrace();
        }
    }

    //STARTS LOCATION LISTENER SERVICE
    private void startLocationListenerService() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationListenerService.class.getName().equals(service.service.getClassName())) {
                Log.d(CLASS_NAME, "FETCHING LOCATION LISTENER SERVICE...");
                bindService(new Intent(this, LocationListenerService.class),
                        connection, Context.BIND_AUTO_CREATE);
                Log.d(CLASS_NAME, "LOCATION LISTENER SERVICE RE-BIND");
                return;
            }
        }

        Intent intent = new Intent(this, LocationListenerService.class);
        intent.setAction("StartLocationService");
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.d(CLASS_NAME, "LOCATION LISTENER SERVICE STARTED");
    }
}