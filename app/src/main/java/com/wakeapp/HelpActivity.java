package com.wakeapp;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTheme();
        setContentView(R.layout.activity_help);
    }

    @Override
    public void onResume() {
        setTitle(getString(R.string.menu_item_help));
        loadTheme();
        super.onResume();
    }

    private void loadTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String appTheme = sp.getString("styleType", "dark");

        if ("dark".equals(appTheme)) {
            setTheme(getResources().getIdentifier("AppThemeAction",
                    "style",
                    getPackageName()));
        } else {
            appTheme = appTheme.substring(0, 1).toUpperCase() + appTheme.substring(1) + "Action";
            setTheme(getResources().getIdentifier(appTheme,
                    "style",
                    getPackageName()));
        }
    }
}