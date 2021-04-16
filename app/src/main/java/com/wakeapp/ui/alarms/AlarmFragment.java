package com.wakeapp.ui.alarms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.wakeapp.R;
import com.wakeapp.models.Alarm.Alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AlarmFragment extends Fragment {

    private static Map<Integer, String> intervals;
    static {
        Map<Integer, String> aMap = new HashMap<>();
        aMap.put(1, "30 Min");
        aMap.put(2, "1 Hour");
        aMap.put(4, "2 Hours");
        aMap.put(6, "3 Hours");
        aMap.put(12, "6 Hours");
        intervals = Collections.unmodifiableMap(aMap);
    }

    private ArrayList<Alarm> alarms;
    private Alarm alarm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                                ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);

        alarms = (ArrayList<Alarm>) getArguments().getSerializable("alarms_list");
        alarm = alarms.get(getArguments().getInt("ALARM_ID", 0));
        System.out.println("\nHERE: " + alarm + " ");

        //Alarm Name
        EditText alarmName = rootView.findViewById(R.id.alarm_name);
        alarmName.setText(alarm.getName());

        //All Time Active
        SwitchCompat allTimeActive = rootView.findViewById(R.id.all_time_active);
        allTimeActive.setChecked(alarm.getTimeActive());

        //Time
        final TextView alarmTimeLabel = rootView.findViewById(R.id.alarm_time_label);
        final EditText alarmTime = rootView.findViewById(R.id.alarm_time);
        final TextView alarmIntervalLabel = rootView.findViewById(R.id.alarm_interval_label);
        final Spinner alarmInterval = rootView.findViewById(R.id.alarm_interval);
        alarmTime.setText(alarm.getTime().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(intervals.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmInterval.setAdapter(adapter);

        if (allTimeActive.isChecked()) {
            alarmTimeLabel.setVisibility(View.GONE);
            alarmTime.setVisibility(View.GONE);
            alarmIntervalLabel.setVisibility(View.GONE);
            alarmInterval.setVisibility(View.GONE);
        }

        allTimeActive.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
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

        //All Days Active

        SwitchCompat allDaysActive = rootView.findViewById(R.id.all_days_active);
        allDaysActive.setChecked(alarm.getDaysActive());

        final ToggleButton tSun = (ToggleButton) rootView.findViewById(R.id.tSun);
        tSun.setChecked(alarm.getDays().get(0));
        final ToggleButton tMon = (ToggleButton) rootView.findViewById(R.id.tMon);
        tMon.setChecked(alarm.getDays().get(1));
        final ToggleButton tTue = (ToggleButton) rootView.findViewById(R.id.tTue);
        tTue.setChecked(alarm.getDays().get(2));
        final ToggleButton tWed = (ToggleButton) rootView.findViewById(R.id.tWed);
        tWed.setChecked(alarm.getDays().get(3));
        final ToggleButton tThu = (ToggleButton) rootView.findViewById(R.id.tThu);
        tThu.setChecked(alarm.getDays().get(4));
        final ToggleButton tFri = (ToggleButton) rootView.findViewById(R.id.tFri);
        tFri.setChecked(alarm.getDays().get(5));
        final ToggleButton tSat = (ToggleButton) rootView.findViewById(R.id.tSat);
        tSat.setChecked(alarm.getDays().get(6));

        if (allDaysActive.isChecked()) {
            tSun.setVisibility(View.GONE);
            tMon.setVisibility(View.GONE);
            tTue.setVisibility(View.GONE);
            tWed.setVisibility(View.GONE);
            tThu.setVisibility(View.GONE);
            tFri.setVisibility(View.GONE);
            tSat.setVisibility(View.GONE);
        }

        allDaysActive.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tSun.setVisibility(View.GONE);
                    tMon.setVisibility(View.GONE);
                    tTue.setVisibility(View.GONE);
                    tWed.setVisibility(View.GONE);
                    tThu.setVisibility(View.GONE);
                    tFri.setVisibility(View.GONE);
                    tSat.setVisibility(View.GONE);
                } else {
                    tSun.setVisibility(View.VISIBLE);
                    tMon.setVisibility(View.VISIBLE);
                    tTue.setVisibility(View.VISIBLE);
                    tWed.setVisibility(View.VISIBLE);
                    tThu.setVisibility(View.VISIBLE);
                    tFri.setVisibility(View.VISIBLE);
                    tSat.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button save = (Button) view.findViewById(R.id.save_button);
        final NavController navController = Navigation.findNavController(view);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarm();
                navController.navigate(R.id.nav_alarms);
            }
        });
    }

    private long intervalMultiplier(final int selected) {
        return selected * 1800000L;
    }

    private void saveAlarm() {

    }
}
