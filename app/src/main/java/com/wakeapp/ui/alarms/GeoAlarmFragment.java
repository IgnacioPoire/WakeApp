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

public class GeoAlarmFragment extends Fragment {

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
    private SwitchCompat timeActive;
    private SwitchCompat daysActive;
    private EditText alarmTime;
    private TimePickerDialog timePickerDialog;
    private Spinner alarmInterval;

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

        //All Time Active
        timeActive = rootView.findViewById(R.id.all_time_active);
        timeActive.setChecked(geoAlarm.getTimeActive());

        //Time
        final TextView alarmTimeLabel = rootView.findViewById(R.id.alarm_time_label);
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
        final TextView alarmIntervalLabel = rootView.findViewById(R.id.alarm_interval_label);
        alarmInterval = rootView.findViewById(R.id.alarm_interval);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(intervals.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmInterval.setAdapter(adapter);
        if (geoAlarm.getInterval() != 0) {
            alarmInterval.setSelection(intervalPos.get(geoAlarm.getInterval()));
        }

        if (timeActive.isChecked()) {
            alarmTimeLabel.setVisibility(View.GONE);
            alarmTime.setVisibility(View.GONE);
            alarmIntervalLabel.setVisibility(View.GONE);
            alarmInterval.setVisibility(View.GONE);
        }

        timeActive.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
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
        final Button save = (Button) view.findViewById(R.id.save_button);
        final Button delete = (Button) view.findViewById(R.id.delete_button);
        final NavController navController = Navigation.findNavController(view);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarm();
                navController.navigate(R.id.nav_alarms);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAlarm();
                navController.navigate(R.id.nav_alarms);
            }
        });
    }

    private void saveAlarm() {
        geoAlarm.setName(alarmName.getText().toString());
        geoAlarm.setTimeActive(timeActive.isChecked());
        if (!(timeActive.isChecked())) {
            String[] time = alarmTime.getText().toString().split(":");
            geoAlarm.setHour(Integer.parseInt(time[0]));
            geoAlarm.setMinutes(Integer.parseInt(time[1]));
            geoAlarm.setInterval(getKeyFromValue(alarmInterval.getSelectedItem().toString()));
            System.out.println("EndTime: " + geoAlarm.getEndHour() + ":" + geoAlarm.getEndMinutes());
        }
        geoAlarm.setDaysActive(daysActive.isChecked());
        if (!(daysActive.isChecked())) {
            geoAlarm.setDays(0, tSun.isChecked());
            geoAlarm.setDays(1, tMon.isChecked());
            geoAlarm.setDays(2, tTue.isChecked());
            geoAlarm.setDays(3, tWed.isChecked());
            geoAlarm.setDays(4, tThu.isChecked());
            geoAlarm.setDays(5, tFri.isChecked());
            geoAlarm.setDays(6, tSat.isChecked());
        }

        geoAlarms.set(getArguments().getInt("ALARM_ID", 0), geoAlarm);
        try {
            checkFileExists();
            File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/alarms.txt");
            FileOutputStream fos = new FileOutputStream(alarmFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(geoAlarms);
            os.close();
            fos.close();
            System.out.print("SAVED " + geoAlarms);
        } catch (FileNotFoundException e) {
            System.out.println("No file found saveChanges");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException in SaveChanges");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer getKeyFromValue(String value) {
        for(Map.Entry<Integer, String> entry: intervals.entrySet()) {

            if(value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return 0;
    }

    private void checkFileExists() {
        File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/alarms.txt");
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

    private void deleteAlarm() {
        geoAlarms.remove(getArguments().getInt("ALARM_ID", 0));
        try {
            checkFileExists();
            File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/alarms.txt");
            FileOutputStream fos = new FileOutputStream(alarmFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(geoAlarms);
            os.close();
            fos.close();
            System.out.print("SAVED " + geoAlarms);
        } catch (FileNotFoundException e) {
            System.out.println("No file found saveChanges");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException in SaveChanges");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
