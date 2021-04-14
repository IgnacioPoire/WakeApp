package com.wakeapp.ui.alarms;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.wakeapp.R;
import com.wakeapp.models.Alarm.Alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AlarmFragment extends Fragment {

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                                ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.alarm_form, container, false);

        Bundle bundle = this.getArguments();
        alarms = (ArrayList<Alarm>) bundle.getSerializable("alarms_list");
        alarm = alarms.get(bundle.getInt("ALARM_ID", 0));
        System.out.println("\nHERE: " + alarm + " ");

        //Alarm Name
        EditText alarmName = rootView.findViewById(R.id.alarm_name);
        alarmName.setText(alarm.getName());

        //Always Active
        SwitchCompat alwaysActive = rootView.findViewById(R.id.always_active);
        alwaysActive.setChecked(alarm.getAlwaysActive());

        //Time
        final TextView alarmTimeLabel = rootView.findViewById(R.id.alarm_time_label);
        final EditText alarmTime = rootView.findViewById(R.id.alarm_time);
        final TextView alarmIntervalLabel = rootView.findViewById(R.id.alarm_interval_label);
        final Spinner alarmInterval = rootView.findViewById(R.id.alarm_interval);
        alarmTime.setText(alarm.getTime().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
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

        return rootView;
    }
}
