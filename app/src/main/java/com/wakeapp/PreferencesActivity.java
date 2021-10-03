package com.wakeapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;

public class PreferencesActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTheme();
        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new PreferenceFragment())
            .commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onResume() {
        setTitle(getString(R.string.menu_item_settings));
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

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        private final ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Uri alarmRingtone = result.getData()
                                    .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                            if (alarmRingtone != null) {
                                final SharedPreferences sp = PreferenceManager
                                        .getDefaultSharedPreferences(requireActivity());
                                @SuppressLint("CommitPrefEdits")
                                final SharedPreferences.Editor editor = sp.edit();

                                editor.putString("alarmRingtone", alarmRingtone.toString());
                                editor.apply();

                                String ringtone = sp.getString("alarmRingtone", null);
                                Log.d("SettingsSaved", ringtone);
                            }
                        }
                    }
                });

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            loadSettings();
        }

        private void loadSettings() {
            final SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(requireActivity());
            @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor editor = sp.edit();

            ListPreference LPMT = findPreference("styleType");
            LPMT.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String styleType = sp.getString("styleType", "standard");
                    if ("black".equals(styleType)) {
                        editor.putString("styleType", "black");
                    } else if ("blue".equals(styleType)) {
                        editor.putString("styleType", "blue");
                    } else if ("dark".equals(styleType)) {
                        editor.putString("styleType", "dark");
                    } else if ("gray".equals(styleType)) {
                        editor.putString("styleType", "gray");
                    } else if ("vintage".equals(styleType)) {
                        editor.putString("styleType", "vintage");
                    }
                    editor.apply();
                    restartActivity();

                    return true;
                }
            });

            ListPreference LPAL = findPreference("appLanguage");
            LPAL.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String appLanguage = sp.getString("appLanguage", "en");
                    if ("en".equals(appLanguage)) {
                        editor.putString("appLanguage", "en");
                    } else if ("es".equals(appLanguage)) {
                        editor.putString("appLanguage", "es");
                    }
                    editor.apply();
                    restartActivity();

                    return true;
                }
            });
        }

        private void restartActivity() {
            requireActivity().finish();
            Intent i = new Intent(requireContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().overridePendingTransition(0, 0);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("alarmRingtone")) {
                openSomeActivityForResult();
                return true;
            } else {
                return super.onPreferenceTreeClick(preference);
            }
        }

        public void openSomeActivityForResult() {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                    true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                    true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = GetRingtonePreferenceValue();
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                            (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                            Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            activityResultLauncher.launch(intent);
        }

        private String GetRingtonePreferenceValue()
        {
            final SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(requireActivity());
            @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor editor = sp.edit();

            return sp.getString("alarmRingtone", null);
        }
    }
}
