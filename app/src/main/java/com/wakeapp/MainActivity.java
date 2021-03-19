package com.wakeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import com.wakeapp.models.Alarm.Alarm;
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
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements VariableInterface {

    private AppBarConfiguration mAppBarConfiguration;
    private ArrayList<Alarm> alarms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFileExists();
        loadAlarms();
        setContentView(R.layout.activity_main);
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
    public ArrayList<Alarm> getAlarmList() {
        return alarms;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //new FileOutputStream(getExternalFilesDir(null) + "/alarms.data");
            //FileOutputStream fos = getApplicationContext().openFileOutput(getFilesDir() + "/alarms.data", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(alarms);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("No file found saveChanges");
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException in SaveChanges");
            System.out.println(e.getMessage());
        }
    }

    private void loadAlarms() {
        try {
            //FileInputStream fin = getApplicationContext().openFileInput(getFilesDir() + "/alarms.data");
            //FileInputStream fin =  new FileInputStream(getExternalFilesDir(null) + "/alarms.data");
            ObjectInputStream is = new ObjectInputStream(fin);
            alarms = (ArrayList<Alarm>) is.readObject();
            if (alarms == null) {
                alarms = new ArrayList<>();
            } else {
                System.out.println(alarms);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found loadAlarms");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException in loadAlarms");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound in loadAlarms");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}