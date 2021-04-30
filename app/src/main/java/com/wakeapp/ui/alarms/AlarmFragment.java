package com.wakeapp.ui.alarms;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.wakeapp.R;
import com.wakeapp.models.alarms.GeoAlarm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

    private static Map<Integer, Integer> intervalPos;
    static {
        Map<Integer, Integer> aMap = new HashMap<>();
        aMap.put(1, 0);
        aMap.put(2, 1);
        aMap.put(4, 2);
        aMap.put(6, 3);
        aMap.put(12, 4);
        intervalPos = Collections.unmodifiableMap(aMap);
    }

    private ArrayList<GeoAlarm> geoAlarms;
    private GeoAlarm geoAlarm;
    private EditText alarmName;
    private SwitchCompat daysActive;
    private EditText alarmTime;
    private TimePickerDialog timePickerDialog;

    private ToggleButton tSun;
    private ToggleButton tMon;
    private ToggleButton tTue;
    private ToggleButton tWed;
    private ToggleButton tThu;
    private ToggleButton tFri;
    private ToggleButton tSat;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_geo_alarm, container, false);

        geoAlarms = (ArrayList<GeoAlarm>) getArguments().getSerializable("alarms_list");
        geoAlarm = geoAlarms.get(getArguments().getInt("ALARM_ID", 0));
        System.out.println("\nHERE: " + geoAlarm + " ");

        //Alarm Name
        alarmName = rootView.findViewById(R.id.alarm_name);
        alarmName.setText(geoAlarm.getName());

        //Time
        alarmTime = rootView.findViewById(R.id.alarm_time);
        alarmTime.setText(String.format("%02d:%02d", geoAlarm.getHour(), geoAlarm.getMinutes()));
        alarmTime.setFocusable(false);
        alarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onTimeSet(TimePicker view, int hours, int minutes) {
                        alarmTime.setText(String.format("%02d:%02d", hours, minutes));
                    }
                }, 0, 0, true);
                timePickerDialog.show();
            }
        });

        //All Days Active

        daysActive = rootView.findViewById(R.id.all_days_active);
        daysActive.setChecked(geoAlarm.getDaysActive());

        tSun = (ToggleButton) rootView.findViewById(R.id.tSun);
        tSun.setChecked(geoAlarm.getDays().get(0));
        tMon = (ToggleButton) rootView.findViewById(R.id.tMon);
        tMon.setChecked(geoAlarm.getDays().get(1));
        tTue = (ToggleButton) rootView.findViewById(R.id.tTue);
        tTue.setChecked(geoAlarm.getDays().get(2));
        tWed = (ToggleButton) rootView.findViewById(R.id.tWed);
        tWed.setChecked(geoAlarm.getDays().get(3));
        tThu = (ToggleButton) rootView.findViewById(R.id.tThu);
        tThu.setChecked(geoAlarm.getDays().get(4));
        tFri = (ToggleButton) rootView.findViewById(R.id.tFri);
        tFri.setChecked(geoAlarm.getDays().get(5));
        tSat = (ToggleButton) rootView.findViewById(R.id.tSat);
        tSat.setChecked(geoAlarm.getDays().get(6));

        if (daysActive.isChecked()) {
            tSun.setVisibility(View.GONE);
            tMon.setVisibility(View.GONE);
            tTue.setVisibility(View.GONE);
            tWed.setVisibility(View.GONE);
            tThu.setVisibility(View.GONE);
            tFri.setVisibility(View.GONE);
            tSat.setVisibility(View.GONE);
        }

        daysActive.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
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
        final Button saveOnly = (Button) view.findViewById(R.id.save_only_button);
        final Button save = (Button) view.findViewById(R.id.save_button);
        final Button delete = (Button) view.findViewById(R.id.delete_button);
        final NavController navController = Navigation.findNavController(view);
    }
}

