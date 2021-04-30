package com.wakeapp.ui.alarms;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.wakeapp.models.alarms.Alarm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AlarmFragment extends Fragment {

    private ArrayList<Alarm> alarms;
    private Alarm alarm;
    private int alarmId;

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

        alarms = (ArrayList<Alarm>) getArguments().getSerializable("alarms_list");
        alarmId = getArguments().getInt("ALARM_ID", -1);
        if (alarmId != -1) {
            alarm = alarms.get(alarmId);
        } else {
            ArrayList<Boolean> days = new ArrayList<>(Arrays.asList(new Boolean[7]));
            Collections.fill(days, Boolean.TRUE);
            alarm = new Alarm("Alarm" + Integer.toString(alarms.size() + 1),
            false,
            12,
            0,
            days);
        }
        System.out.println("\nHERE: " + alarms + " ");

        //Alarm Name
        alarmName = rootView.findViewById(R.id.alarm_name);
        alarmName.setText(alarm.getName());

        //Time
        alarmTime = rootView.findViewById(R.id.alarm_time);
        alarmTime.setText(String.format("%02d:%02d", alarm.getHour(), alarm.getMinutes()));
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
        daysActive.setChecked(alarm.getDaysActive());

        tSun = (ToggleButton) rootView.findViewById(R.id.tSun);
        tSun.setChecked(alarm.getDays().get(0));
        tMon = (ToggleButton) rootView.findViewById(R.id.tMon);
        tMon.setChecked(alarm.getDays().get(1));
        tTue = (ToggleButton) rootView.findViewById(R.id.tTue);
        tTue.setChecked(alarm.getDays().get(2));
        tWed = (ToggleButton) rootView.findViewById(R.id.tWed);
        tWed.setChecked(alarm.getDays().get(3));
        tThu = (ToggleButton) rootView.findViewById(R.id.tThu);
        tThu.setChecked(alarm.getDays().get(4));
        tFri = (ToggleButton) rootView.findViewById(R.id.tFri);
        tFri.setChecked(alarm.getDays().get(5));
        tSat = (ToggleButton) rootView.findViewById(R.id.tSat);
        tSat.setChecked(alarm.getDays().get(6));

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
        alarm.setName(alarmName.getText().toString());
        String[] time = alarmTime.getText().toString().split(":");
        alarm.setHour(Integer.parseInt(time[0]));
        alarm.setMinutes(Integer.parseInt(time[1]));
        alarm.setDaysActive(daysActive.isChecked());
        if (!(daysActive.isChecked())) {
            ArrayList<Boolean> days = new ArrayList<>(Arrays.asList(
                    tSun.isChecked(),
                    tMon.isChecked(),
                    tTue.isChecked(),
                    tWed.isChecked(),
                    tThu.isChecked(),
                    tFri.isChecked(),
                    tSat.isChecked())
            );
            alarm.setDays(days);
        }

        alarms.set(getArguments().getInt("ALARM_ID", 0), alarm);
        try {
            checkFileExists();
            File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/alarms.txt");
            FileOutputStream fos = new FileOutputStream(alarmFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(alarms);
            os.close();
            fos.close();
            System.out.print("SAVED " + alarms);
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
        alarms.remove(getArguments().getInt("ALARM_ID", 0));
        try {
            checkFileExists();
            File alarmFile = new File(getActivity().getExternalFilesDir(null) + "/alarms.txt");
            FileOutputStream fos = new FileOutputStream(alarmFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(alarms);
            os.close();
            fos.close();
            System.out.print("SAVED " + alarms);
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

