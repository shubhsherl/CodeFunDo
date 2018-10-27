package com.shubh.watcherth.ReportFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import com.shubh.watcherth.Dashboard;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.Choice;
import com.shubh.watcherth.ReportObject.IncidentReport;
import com.shubh.watcherth.ReportObject.Report;

public class FloodEmergency extends Fragment {
    private static final String REPORT = "report";
    private IncidentReport incidentReport;
    private static final String TAG = "FloodEmergency";
    private Report flood;

    private int[] radioId = new int[]{R.id.radio_q1_a, R.id.radio_q1_b, R.id.radio_q1_c};
    private int[] checkId = new int[]{R.id.checkbox_q2_a, R.id.checkbox_q2_b, R.id.checkbox_q2_c};
   public Button nextButton;

    public FloodEmergency() {
        // Required empty public constructor
    }

    public static FloodEmergency newInstance(IncidentReport report) {
        FloodEmergency fragment = new FloodEmergency();
        Bundle args = new Bundle();
        args.putParcelable(REPORT, report);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            incidentReport = getArguments().getParcelable(REPORT);
        }
        else incidentReport = new IncidentReport();
        incidentReport = Dashboard.incidentReport;
        flood = incidentReport.getReport(Constant.FLOOD);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Flood");
        setButtonListener();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null) {
            for (int i = 0; i < radioId.length; i++) {
                RadioButton radio = getRadioButton(radioId[i]);
                Log.d(TAG, " id is " + radioId[i] + "");
                int id = savedInstanceState.getInt(radioId[i] + "");
                if (id != 0)
                    radio.setChecked(true);
            }
        }
    }

    public void setButtonListener(){
        RadioButton[] r = new RadioButton[radioId.length];
        RadioButton[] c = new RadioButton[checkId.length];

        for(int i = 0; i < r.length; i++){
            r[i] = getRadioButton(radioId[i]);
            final int index = r[i].getId();
            r[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                onButtonClick(index);
                }
            });
        }

        for(int i = 0; i < c.length; i++){
            c[i] = getRadioButton(checkId[i]);
            final int index = c[i].getId();
            c[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    onButtonClick(index);
                }
            });
        }

        nextButton = (Button) this.getView().findViewById(R.id.button_next_Emergency);
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
               onButtonClick(nextButton.getId());
            }
        });
    }

    public void onButtonClick(int buttonid){
        RadioButton radio;
        RadioButton checkBox;

        switch (buttonid){
            case R.id.radio_q1_a:
            case R.id.radio_q1_b:
            case R.id.radio_q1_c:
                radio = getRadioButton(buttonid);
                if(radio.isChecked())
                    flood.setSingleChoice(0, new Choice(radio.getText().toString(), null));
                else
                    flood.removeOneChoiceQuestion(0);
                break;
            case R.id.checkbox_q2_a:
            case R.id.checkbox_q2_b:
            case R.id.checkbox_q2_c:
                checkBox = getRadioButton(buttonid);
                if(checkBox.isChecked())
                    flood.setMultiChoice(1, new Choice(checkBox.getText().toString(), null));
                else
                    flood.removeMultiChoiceQuestion(1, new Choice(checkBox.getText().toString()));
                break;
            case R.id.button_next_Emergency:
                Fragment fragment = Review.newInstance(incidentReport);
                FragmentTransaction ft = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrame, fragment)
                        .addToBackStack("FloodEmergency");
                ft.commit();
                break;
        }
    }

    public RadioButton getRadioButton(int id){
        return (RadioButton) this.getView().findViewById(id);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }
}
