package com.wakeapp.ui.alarms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.wakeapp.R;
import com.wakeapp.VariableInterface;
import com.wakeapp.models.Alarm.Alarm;
import java.util.ArrayList;

public class AlarmsFragment extends Fragment {

    private VariableInterface varListener;
    private ArrayList<String> alarmsList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VariableInterface) {
            varListener = (VariableInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        varListener = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_alarms, container, false);
        ListView alarmList = rootView.findViewById(R.id.alarmList);

        alarmsList = new ArrayList<>();
        if (!varListener.getAlarmList().isEmpty()) {
            loadAlarms();
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, alarmsList);
        alarmList.setAdapter(arrayAdapter);

        alarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), AlarmActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void loadAlarms() {
        ArrayList<Alarm> alarms = varListener.getAlarmList();
        for (int i = 0; i<alarms.size(); i++) {
            alarmsList.add(alarms.get(i).getName());
        }
    }
}