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

import com.shubh.watcherth.BaseClass.BaseXBeeFragment;
import com.shubh.watcherth.Dashboard;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.Choice;
import com.shubh.watcherth.ReportObject.IncidentReport;
import com.shubh.watcherth.ReportObject.Report;

public class LandslideEmergency extends BaseXBeeFragment {
    private static final String REPORT = "report";
    private IncidentReport incidentReport;
    private static final String TAG = "LandslideEmergency";
    private Report landslide;

    int[] radioId = new int[]{R.id.radio_q1_a, R.id.radio_q1_b, R.id.radio_q1_c};
    int[] checkId = new int[]{R.id.checkbox_q2_a, R.id.checkbox_q2_b, R.id.checkbox_q2_c};

    public Button nextButton;



    public LandslideEmergency() {
        // Required empty public constructor
    }

    public static LandslideEmergency newInstance(IncidentReport param1) {
        LandslideEmergency fragment = new LandslideEmergency();
        Bundle args = new Bundle();
        args.putParcelable(REPORT, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            incidentReport = getArguments().getParcelable(REPORT);
            Log.d(TAG, incidentReport.toString());
        }
        else incidentReport = new IncidentReport();
        incidentReport = Dashboard.incidentReport;
        landslide = incidentReport.getReport(Constant.LANDSLIDE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emergency_question, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, incidentReport.toString());
        getActivity().setTitle("Landslide");
        setButtonListener();
    }

    public void setButtonListener(){
        RadioButton[] r = new RadioButton[radioId.length];
        RadioButton[] c = new RadioButton[checkId.length];

        for(int i = 0; i < radioId.length; i++){
            r[i] = getRadioButton(radioId[i]);
            final int index = r[i].getId();
            r[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    onButtonClick(index);
                }
            });
        }

        for(int i = 0; i < checkId.length; i++){
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

    public RadioButton getRadioButton(int id){
        return (RadioButton) this.getView().findViewById(id);
    }

    public void onButtonClick(int buttonid){
        RadioButton radio;
        RadioButton checkBox = null;
        switch (buttonid){
            case R.id.radio_q1_a:
            case R.id.radio_q1_b:
            case R.id.radio_q1_c:
                radio = (RadioButton) this.getView().findViewById(buttonid);
                if(radio.isChecked()) {
                    landslide.setSingleChoice(0, new Choice(radio.getText().toString(), null));
                    Log.d(TAG, "is checked " + radio.isChecked());
                }
                else {
                    radio.setChecked(false);
                    Log.d(TAG, "is not checked" + radio.isChecked());
                    landslide.removeOneChoiceQuestion(0);
                }
                break;
            case R.id.checkbox_q2_a:
            case R.id.checkbox_q2_b:
            case R.id.checkbox_q2_c:
                checkBox = (RadioButton) this.getView().findViewById(buttonid);
                if (checkBox.isChecked())
                    landslide.setMultiChoice(1, new Choice(checkBox.getText().toString(), null));
                else
                    landslide.removeMultiChoiceQuestion(1, new Choice(checkBox.getText().toString(), null));
                break;
            case R.id.button_next_Emergency:
                Fragment fragment = Review.newInstance(incidentReport);
                FragmentTransaction ft = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrame, fragment)
                        .addToBackStack("Landslide");
                ft.commit();
                break;

        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }
}
