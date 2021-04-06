package com.wakeapp.ui.alarms;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.wakeapp.R;
import com.wakeapp.models.Alarm.Alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AlarmActivity extends AppCompatActivity {

    private static Map<String, Long> intervals;
    static {
        Map<String, Long> aMap = new HashMap<>();
        aMap.put("30 Min", 1800000L);
        aMap.put("1 Hour", 3600000L);
        aMap.put("2 Hour", 7200000L);
        aMap.put("3 Hour", 10800000L);
        aMap.put("6 Hour", 21600000L);
        intervals = Collections.unmodifiableMap(aMap);
    }

    private ArrayList<Alarm> alarms;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarms = (ArrayList<Alarm>) getIntent().getSerializableExtra("alarms_list");
        alarm = alarms.get(getIntent().getIntExtra("ALARM_ID", 0));
        System.out.println("\nHERE: " + alarm + " ");

        setContentView(R.layout.alarm_form);

        //Alarm Name
        EditText alarmName = findViewById(R.id.alarm_name);
        alarmName.setText(alarm.getName());

        //Always Active
        SwitchCompat alwaysActive = findViewById(R.id.always_active);
        alwaysActive.setChecked(alarm.getAlwaysActive());

        //Time
        final TextView alarmTimeLabel = findViewById(R.id.alarm_time_label);
        final EditText alarmTime = findViewById(R.id.alarm_time);
        final TextView alarmIntervalLabel = findViewById(R.id.alarm_interval_label);
        final Spinner alarmInterval = findViewById(R.id.alarm_interval);
        alarmTime.setText(alarm.getTime().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>(intervals.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmInterval.setAdapter(adapter);

        if (alwaysActive.isChecked()) {
            alarmTimeLabel.setVisibility(View.GONE);
            alarmTime.setVisibility(View.GONE);
            alarmIntervalLabel.setVisibility(View.GONE);
            alarmInterval.setVisibility(View.GONE);
        }

        alwaysActive.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    alarmTimeLabel.setVisibility(View.GONE);
                    alarmTime.setVisibility(View.GONE);
                    alarmIntervalLabel.setVisibility(View.GONE);
                    alarmInterval.setVisibility(View.GONE);
                } else {
                    alarmTimeLabel.setVisibility(View.VISIBLE);
                    alarmTime.setVisibility(View.VISIBLE);
                    alarmIntervalLabel.setVisibility(View.VISIBLE);
                    alarmInterval.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
