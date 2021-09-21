package com.wakeapp.ui.alarms;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.wakeapp.R;
import com.wakeapp.auxiliar.AlarmReceiver;
import com.wakeapp.models.alarms.Alarm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

        final View rootView = inflater.inflate(R.layout.fragment_alarm,
                container,
                false);

        alarms = (ArrayList<Alarm>) getArguments().getSerializable("alarms_list");
        alarmId = getArguments().getInt("ALARM_ID", -1);
        if (alarmId != -1) {
            rootView.findViewById(R.id.save_alarm).setVisibility(View.GONE);
            alarm = alarms.get(alarmId);
        } else {
            rootView.findViewById(R.id.save_delete_alarm).setVisibility(View.GONE);
            ArrayList<Boolean> days = new ArrayList<>(Arrays.asList(new Boolean[7]));
            Collections.fill(days, Boolean.TRUE);
            int intName = alarms != null ? alarms.size() + 1 : 1;
            alarm = new Alarm("Alarm " + Integer.toString(intName),
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
                timePickerDialog = new TimePickerDialog(requireActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
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

        tSun = rootView.findViewById(R.id.tSun);
        tSun.setChecked(alarm.getDays().get(0));
        tMon = rootView.findViewById(R.id.tMon);
        tMon.setChecked(alarm.getDays().get(1));
        tTue = rootView.findViewById(R.id.tTue);
        tTue.setChecked(alarm.getDays().get(2));
        tWed = rootView.findViewById(R.id.tWed);
        tWed.setChecked(alarm.getDays().get(3));
        tThu = rootView.findViewById(R.id.tThu);
        tThu.setChecked(alarm.getDays().get(4));
        tFri = rootView.findViewById(R.id.tFri);
        tFri.setChecked(alarm.getDays().get(5));
        tSat = rootView.findViewById(R.id.tSat);
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
        final Button saveOnly = (Button) view.findViewById(R.id.save_only_button);
        final Button save = (Button) view.findViewById(R.id.save_button);
        final Button delete = (Button) view.findViewById(R.id.delete_button);
        final NavController navController = Navigation.findNavController(view);
        saveOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData();
                setScheduledAlarm();
                alarms.add(alarm);
                saveAlarms();
                navController.navigate(R.id.nav_alarms);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseAllScheduledAlarms();
                setData();
                setScheduledAlarm();
                alarms.set(getArguments().getInt("ALARM_ID", 0), alarm);
                saveAlarms();
                navController.navigate(R.id.nav_alarms);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseAllScheduledAlarms();
                deleteAlarm();
                navController.navigate(R.id.nav_alarms);
            }
        });
    }

    private void setData() {
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
    }

    private void setScheduledAlarm() {
        if (!(alarm.getDaysActive())) {
            setDaysAlarmSchedule(alarm.getDays());
        } else {
            setAllDaysAlarmSchedule();
        }
    }

    private void setAllDaysAlarmSchedule() {
        Calendar calendar = Calendar.getInstance();

        Log.d("AlarmT",
                "Alarm Calendar set: "
                + alarm.getHour() + ":"
                + alarm.getMinutes());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.setAction("ALARM_TRIGGER");
        intent.putExtra("ALARM_NAME", alarm.getName());
        intent.putExtra("ALARM_TYPE", "ALARM");
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(requireContext(),
                        alarm.getId(),
                        intent,
                        0);

        AlarmManager alarmManager = (AlarmManager) requireContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
        Log.d("AlarmT", "Saved Scheduled Alarm");
    }

    private void setDaysAlarmSchedule(ArrayList<Boolean> days) {
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i)) {
                switch (i) {
                    case 0:
                        scheduleAlarm(Calendar.SUNDAY);
                        break;
                    case 1:
                        scheduleAlarm(Calendar.MONDAY);
                        break;
                    case 2:
                        scheduleAlarm(Calendar.TUESDAY);
                        break;
                    case 3:
                        scheduleAlarm(Calendar.WEDNESDAY);
                        break;
                    case 4:
                        scheduleAlarm(Calendar.THURSDAY);
                        break;
                    case 5:
                        scheduleAlarm(Calendar.FRIDAY);
                        break;
                    case 6:
                        scheduleAlarm(Calendar.SATURDAY);
                        break;
                }
            }
        }
    }

    private void scheduleAlarm(int day) {

        Log.d("AlarmT", "Alarm Calendar set: "
                + day + "at" + alarm.getHour() + ":" + alarm.getMinutes());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.setAction("ALARM_TRIGGER");
        intent.putExtra("ALARM_NAME", alarm.getName());
        intent.putExtra("ALARM_TYPE", "ALARM");
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(requireContext(),
                        alarm.getId(),
                        intent,
                        0);

        AlarmManager alarmManager = (AlarmManager) requireContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent);
        Log.d("AlarmT", "Saved Scheduled Day Alarm");
    }

    private void eraseAllScheduledAlarms() {
        Alarm alarmToErase = alarms.get(getArguments().getInt("ALARM_ID", 0));
        if (!(alarmToErase.getDaysActive())) {
            eraseDaysAlarmSchedule(alarmToErase);
        } else {
            eraseAllDaysAlarmSchedule(alarmToErase);
        }
    }

    private void eraseAllDaysAlarmSchedule(Alarm alarmToErase) {
        Calendar calendar = Calendar.getInstance();

        Log.d("AlarmT", "Alarm Calendar erase: "
                + alarmToErase.getHour() + ":" + alarmToErase.getMinutes());
        calendar.set(Calendar.HOUR_OF_DAY, alarmToErase.getHour());
        calendar.set(Calendar.MINUTE, alarmToErase.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(requireContext(),
                        alarmToErase.getId(),
                        intent,
                        0);

        AlarmManager alarmManager = (AlarmManager) requireContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d("AlarmT", "Erased Scheduled Alarm");
    }

    private void eraseDaysAlarmSchedule(Alarm alarmToErase) {
        ArrayList<Boolean> days = alarmToErase.getDays();
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i)) {
                switch (i) {
                    case 0:
                        eraseScheduleAlarm(Calendar.SUNDAY, alarmToErase);
                        break;
                    case 1:
                        eraseScheduleAlarm(Calendar.MONDAY, alarmToErase);
                        break;
                    case 2:
                        eraseScheduleAlarm(Calendar.TUESDAY, alarmToErase);
                        break;
                    case 3:
                        eraseScheduleAlarm(Calendar.WEDNESDAY, alarmToErase);
                        break;
                    case 4:
                        eraseScheduleAlarm(Calendar.THURSDAY, alarmToErase);
                        break;
                    case 5:
                        eraseScheduleAlarm(Calendar.FRIDAY, alarmToErase);
                        break;
                    case 6:
                        eraseScheduleAlarm(Calendar.SATURDAY, alarmToErase);
                        break;
                }
            }
        }
    }

    private void eraseScheduleAlarm(int day, Alarm alarmToErase) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, alarmToErase.getHour());
        calendar.set(Calendar.MINUTE, alarmToErase.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(requireContext(),
                        alarmToErase.getId(),
                        intent,
                        0);

        AlarmManager alarmManager = (AlarmManager) requireContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void saveAlarms() {
        try {
            checkFileExists();
            File alarmFile = new File(requireActivity()
                    .getExternalFilesDir(null) + "/alarms.txt");
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
        File alarmFile = new File(requireActivity()
                .getExternalFilesDir(null) + "/alarms.txt");
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
            File alarmFile = new File(requireActivity()
                    .getExternalFilesDir(null) + "/alarms.txt");
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

