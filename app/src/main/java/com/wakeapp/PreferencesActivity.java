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

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class PreferencesActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new PreferenceFragment())
            .commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onResume() {
        setTitle(getString(R.string.menu_item_settings));
        super.onResume();
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        private static final int REQUEST_CODE_ALERT_RINGTONE = 505;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            loadSettings();
        }

        private void loadSettings() {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor editor = sp.edit();

            ListPreference LPMT = findPreference("mapType");
            LPMT.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String mapType = sp.getString("mapType", "standard");
                    if ("black".equals(mapType)) {
                        editor.putString("mapType", "black");
                    } else if ("blue".equals(mapType)) {
                        editor.putString("mapType", "blue");
                    } else if ("dark".equals(mapType)) {
                        editor.putString("mapType", "dark");
                    } else if ("gray".equals(mapType)) {
                        editor.putString("mapType", "gray");
                    } else if ("vintage".equals(mapType)) {
                        editor.putString("mapType", "vintage");
                    }
                    editor.apply();

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
            getActivity().finish();
            Intent i = new Intent(getContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            getActivity().overridePendingTransition(0, 0);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("alarmRingtone")) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                String existingValue = GetRingtonePreferenceValue();
                if (existingValue != null) {
                    if (existingValue.length() == 0) {
                        // Select "Silent"
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                    } else {
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                    }
                } else {
                    // No ringtone has been selected, set to the default
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                }

                startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
                return true;
            } else {
                return super.onPreferenceTreeClick(preference);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
                Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (ringtone != null) {
                    SetRingtonePreferenceValue(ringtone.toString());
                } else {
                    SetRingtonePreferenceValue("");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

        private void SetRingtonePreferenceValue(String ringtone)
        {
            Context context = getActivity();
            assert context != null;
            context.getSharedPreferences("alarmRingtone", Context.MODE_PRIVATE).edit().putString("alarmRingtone", ringtone).apply();
        }

        private String GetRingtonePreferenceValue()
        {
            Context context = getActivity();
            assert context != null;
            return context.getSharedPreferences("alarmRingtone", Context.MODE_PRIVATE).getString("alarmRingtone", "content://settings/system/notification_sound");
        }
    }
}
