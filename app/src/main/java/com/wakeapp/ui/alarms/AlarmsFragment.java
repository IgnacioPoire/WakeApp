package com.wakeapp.ui.alarms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.wakeapp.R;
import com.wakeapp.VariableInterface;
import com.wakeapp.models.Alarm.Alarm;

import java.util.ArrayList;

public class AlarmsFragment extends Fragment {

    private VariableInterface varListener;
    private ArrayList<Alarm> alarmsList;
    private ListView alarmList;
    private ArrayList<String> alarmsStrings;

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
        alarmList = rootView.findViewById(R.id.alarmList);

        alarmsStrings = new ArrayList<>();
        if (!varListener.getAlarmList().isEmpty()) {
            loadAlarms();
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, alarmsStrings);
        alarmList.setAdapter(arrayAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);
        alarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("alarms_list", alarmsList);
                bundle.putInt("ALARM_ID", position);
                navController.navigate(R.id.nav_alarm, bundle);
            }
        });
    }

    private void loadAlarms() {
        alarmsList = varListener.getAlarmList();
        for (int i = 0; i<alarmsList.size(); i++) {
            alarmsStrings.add(alarmsList.get(i).getName());
        }
    }
}