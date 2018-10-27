package com.shubh.watcherth.ReportFragments;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.shubh.watcherth.ReportObject.Utils;
import com.shubh.watcherth.dialog.WarningDialog;

import java.util.ArrayList;

public class IncidentType extends BaseXBeeFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    private OnFragmentInteractionListener mListener;
    public Button nextButton;
    private boolean falseReportWarning = false;
    private IncidentReport incidentReport;
    private static final String REPORT = "report";
    private static final String TAG = "IncidentType";
    private int[] id = new int[]{R.id.radio_incidentType_emergency, R.id.radio_incidentType_cyclone, R.id.radio_incidentType_flood,
            R.id.radio_incidentType_volcano, R.id.radio_incidentType_landslide, R.id.radio_incidentType_earthquake,
            R.id.radio_incidentType_other, R.id.button_next_incidentType};
    private Fragment[] fragments = new Fragment[6];

    public IncidentType() {
        // Required empty public constructor
    }

    public static IncidentType newInstance(String param1, String param2) {
        IncidentType fragment = new IncidentType();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        if (incidentReport == null)
            incidentReport = Dashboard.incidentReport;

        for (int i = 0; i < fragments.length; i++) {
            switch (i) {
                case Constant.CYCLONE:
                    fragments[Constant.CYCLONE] = new CycloneEmergency();
                    break;
                case Constant.FLOOD:
                    fragments[Constant.FLOOD] = new FloodEmergency();
                    break;
                case Constant.VOLCANO:
                    fragments[Constant.VOLCANO] = new VolcanoEmergency();
                    break;
                case Constant.LANDSLIDE:
                    fragments[Constant.LANDSLIDE] = new LandslideEmergency();
                    break;
                case Constant.EARTHQUAKE:
                    fragments[Constant.EARTHQUAKE] = new EarthquakeEmergency();
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incident_type, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Choose Incident Type");
        setButtonListener();
        showWarningDialog();

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    public void setButtonListener() {
        RadioButton[] b = new RadioButton[7];
        for (int i = 0; i < 6; i++) {
            b[i] = getRadioButtonButton(id[i]);
            final int index = b[i].getId();
            b[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    onButtonClick(index);
                }
            });
        }
        nextButton = (Button) this.getView().findViewById(R.id.button_next_incidentType);
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onButtonClick(nextButton.getId());
            }
        });
    }

    public RadioButton getRadioButtonButton(int id) {
        return (RadioButton) this.getView().findViewById(id);
    }

    public void startFragment(Fragment fragment, int i) {
        if (fragment == null)
            fragment = makeFragment(i);
        FragmentTransaction ft = getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrame, fragment)
                .addToBackStack("incidentType");
        ft.commit();
    }

    public Fragment makeFragment(int i) {
        switch (i) {
            case Constant.CYCLONE:
                return new CycloneEmergency();
            case Constant.FLOOD:
                return new FloodEmergency();
            case Constant.VOLCANO:
                return new VolcanoEmergency();
            case Constant.LANDSLIDE:
                return new LandslideEmergency();
            case Constant.EARTHQUAKE:
                return new EarthquakeEmergency();
        }
        return null;
    }

    public void showTrappedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this.getContext()).create();
        alertDialog.setTitle("Emergency");
        alertDialog.setMessage("Emergency?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Report emergency = incidentReport.getReport(Constant.EMERGENCY);
                        emergency.setSingleChoice(0, new Choice("Yes", null));
                        startFragment(fragments[Constant.CYCLONE], Constant.CYCLONE);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void onButtonClick(int buttonid) {
        switch (buttonid) {
            case R.id.radio_incidentType_emergency:
                if (getRadioButtonButton(buttonid).isChecked())
                    showTrappedDialog();
                else
                    incidentReport.getReport(Constant.EMERGENCY).removeSingleChoice(0, new Choice(getRadioButtonButton(buttonid).getText().toString()));
                break;
            case R.id.radio_incidentType_cyclone:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildCycloneReport(), Constant.CYCLONE);
                    fragments[Constant.CYCLONE] = null;
                } else
                    startFragment(fragments[Constant.CYCLONE], Constant.CYCLONE);
                break;
            case R.id.radio_incidentType_flood:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildFloodReport(), Constant.FLOOD);
                    fragments[Constant.FLOOD] = null;
                } else
                    startFragment(fragments[Constant.FLOOD], Constant.FLOOD);
                break;
            case R.id.radio_incidentType_volcano:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildVolcanoReport(), Constant.VOLCANO);
                    fragments[Constant.VOLCANO] = null;
                } else
                    startFragment(fragments[Constant.VOLCANO], Constant.VOLCANO);
                break;
            case R.id.radio_incidentType_landslide:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildLandslideReport(), Constant.LANDSLIDE);
                    fragments[Constant.LANDSLIDE] = null;
                } else
                    startFragment(fragments[Constant.LANDSLIDE], Constant.LANDSLIDE);
                break;
            case R.id.radio_incidentType_earthquake:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildEarthquakeReport(), Constant.EARTHQUAKE);
                    fragments[Constant.EARTHQUAKE] = null;
                } else
                    startFragment(fragments[Constant.EARTHQUAKE], Constant.EARTHQUAKE);
                break;
            case R.id.radio_incidentType_other:
                if (!getRadioButtonButton(buttonid).isChecked()) {
                    incidentReport.setReport(Utils.buildCycloneReport(), Constant.CYCLONE);
                    fragments[Constant.CYCLONE] = null;
                } else
                    startFragment(fragments[Constant.CYCLONE], Constant.CYCLONE);
                break;
            case R.id.button_next_incidentType:
                CharSequence[] items = getCheckedChoice();
                if (items.length > 0)
                    showNavigationDialog(items);
                else
                    startFragment(fragments[Constant.CYCLONE], Constant.CYCLONE);
                break;
        }
    }

    public CharSequence[] getCheckedChoice() {
        final ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i < id.length - 1; i++) {
            RadioButton box = (RadioButton) getView().findViewById(id[i]);
            if (box.isChecked()) {
                items.add(box.getText().toString());
            }
        }
        final CharSequence[] itemsList = new CharSequence[items.size() + 1];
        for (int i = 0; i < items.size(); i++) {
            itemsList[i] = items.get(i);
        }
        itemsList[itemsList.length - 1] = "Review";
        return itemsList;
    }

    public void showNavigationDialog(final CharSequence[] itemsList) {
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Go To...")
                .setSingleChoiceItems(itemsList, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = itemsList[which].toString();
                        switch (text) {
                            case "Cyclone":
                                startFragment(fragments[Constant.CYCLONE], Constant.CYCLONE);
                                break;
                            case "Flood":
                                startFragment(fragments[Constant.FLOOD], Constant.FLOOD);
                                break;
                            case "Volcano":
                                startFragment(fragments[Constant.VOLCANO], Constant.VOLCANO);
                                break;
                            case "Landslide":
                                startFragment(fragments[Constant.LANDSLIDE], Constant.LANDSLIDE);
                                break;
                            case "Earthquake":
                                startFragment(fragments[Constant.EARTHQUAKE], Constant.EARTHQUAKE);
                                break;
                            case "Review":
                                Fragment fragment = Review.newInstance(incidentReport);
                                FragmentTransaction ft = getActivity()
                                        .getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.mainFrame, fragment)
                                        .addToBackStack("Emergency");
                                ft.commit();
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public void showWarningDialog() {

        if (!falseReportWarning) {
            WarningDialog alert = new WarningDialog();
            alert.showDialog(getActivity(), "False report will be prosecuted.");
            falseReportWarning = true;
        }
    }

}