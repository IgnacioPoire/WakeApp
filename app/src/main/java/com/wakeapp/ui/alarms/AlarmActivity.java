package com.wakeapp.ui.alarms;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.wakeapp.R;
import com.wakeapp.models.Alarm.Alarm;

import java.util.ArrayList;

public class AlarmActivity extends AppCompatActivity {

    private ArrayList<Alarm> alarms;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println(getIntent().getIntExtra("ALARM_ID", 0));
        alarms = (ArrayList<Alarm>) getIntent().getSerializableExtra("alarms_list");
        alarm = alarms.get(getIntent().getIntExtra("id_alarm", 0));
        EditText e = (EditText) findViewById(R.id.alarm_name);
        e.setText(alarm.getName());
        setContentView(R.layout.alarm_form);
    }
}
