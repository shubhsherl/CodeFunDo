package com.shubh.watcherth.ReportFragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.shubh.watcherth.BluetoothChatService;
import com.shubh.watcherth.BluetoothDeviceListActivity;
import com.shubh.watcherth.Dashboard;
import com.shubh.watcherth.LocationUtils.GeoConstant;
import com.shubh.watcherth.LocationUtils.GeocodeIntentService;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.ReportObject.IncidentReport;
import com.shubh.watcherth.ReportObject.Packet;
import com.shubh.watcherth.ReportObject.Utils;
import com.shubh.watcherth.db.handler.DatabaseHandler;
import com.shubh.watcherth.db.model.Report;
import com.shubh.watcherth.util.CompactReportUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class Review extends Fragment {

    private static final String REPORT = "report";
    private IncidentReport incidentReport;
    private static final String TAG = "Review";
    private DatabaseReference db;
    private AddressResultReceiver resultReceiver;
    private Location location;
    private Address address;
    private String phoneNumber;
    Button submit;

    private int sendingMethod = 0;
    private int receivingMethod = 0;

    private TextView selectMethodTextView;

    private static int REQUEST_ENABLE_BT = 100;
    private static int REQUEST_SELECT_BT = 101;

    // Message types sent from the BluetoothMessageService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothMessageService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothChatService mChatService = null;

    public Review() {
        // Required empty public constructor
    }

    public static Review newInstance(IncidentReport report) {
        Review fragment = new Review();
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
        }else {
            incidentReport = new IncidentReport();
        }

        setHasOptionsMenu(true);

        incidentReport = Dashboard.incidentReport;
        db = FirebaseDatabase.getInstance().getReference("Reports");

        resultReceiver = new AddressResultReceiver(null);
    }


    public void getAddress(){
        Log.d(TAG, "Starting service");
        Intent intent = new Intent(this.getActivity(), GeocodeIntentService.class);
        intent.putExtra(GeoConstant.RECEIVER, resultReceiver);
        intent.putExtra(GeoConstant.FETCH_TYPE_EXTRA, GeoConstant.COORDINATE);
        intent.putExtra(GeoConstant.LOCATION_DATA_EXTRA, location);
        Log.d(TAG, location.getLongitude()+"");
        getActivity().startService(intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Review");

        submit = (Button) this.getView().findViewById(R.id.button_review_submit);

        TextView emergency = new TextView(getContext());
        TextView cyclone = new TextView(getContext());
        TextView flood = new TextView(getContext());
        TextView volcano = new TextView(getContext());
        TextView earthquake = new TextView(getContext());
        TextView landslide = new TextView(getContext());

        formatReviewInformationTextView(emergency);
        formatReviewInformationTextView(cyclone);
        formatReviewInformationTextView(flood);
        formatReviewInformationTextView(volcano);
        formatReviewInformationTextView(earthquake);
        formatReviewInformationTextView(landslide);

        emergency.setText(incidentReport.getReport(Constant.EMERGENCY).toString());
        cyclone.setText(incidentReport.getReport(Constant.CYCLONE).toString());
        flood.setText(incidentReport.getReport(Constant.FLOOD).toString());
        volcano.setText(incidentReport.getReport(Constant.VOLCANO).toString());
        earthquake.setText(incidentReport.getReport(Constant.EARTHQUAKE).toString());
        landslide.setText(incidentReport.getReport(Constant.LANDSLIDE).toString());

        LinearLayout layout = (LinearLayout) this.getView().findViewById(R.id.view_review);

        if(!emergency.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.EMERGENCY);
        }
        if(!cyclone.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.CYCLONE);
        }
        if(!flood.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.FLOOD);
        }
        if(!volcano.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.VOLCANO);
        }
        if(!earthquake.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.EARTHQUAKE);
        }
        if(!landslide.getText().toString().isEmpty()) {
            setReviewInformationOnScreen(layout, Constant.LANDSLIDE);
        }

        selectMethodTextView = (TextView) view.findViewById(R.id.selectMethodMessage);

        setupSendButtonOptions();
        phoneNumber = Dashboard.phoneNumber;
        //getphoneNumber();
        setButtonListener();
    }

    public void setReviewInformationOnScreen(LinearLayout layout, int reportType){

        CardView cardView = (CardView) LayoutInflater.from(getContext())
                .inflate(R.layout.row_review_list, null, false);

        TextView reportTitle = (TextView) cardView.findViewById(R.id.reviewTitleTextView);

        String reportCategory = incidentReport.getReport(reportType).getType();
        reportTitle.setText(reportCategory);

        TextView reportInformation = (TextView) cardView.findViewById(R.id.reviewInformationTextView);

        String info = incidentReport.getReport(reportType).toString();
        info = info.replace(reportCategory.toUpperCase()+"\n","");

        reportInformation.setText(info);
        layout.addView(cardView);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    public void getphoneNumber(){
        //get phone number from sharedPreference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        phoneNumber = "";
        phoneNumber = sharedPreferences.getString(Constant.PHONE_NUMBER, phoneNumber);
        if (phoneNumber==null)
            phoneNumber = "";
    }

    public void setButtonListener(){

        submit.setEnabled(false);

        submit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showSubmitConfirmationDialog();
            }
        });
                if(checkInternetConnection()) {
                    selectMethodTextView.setText("Send Over Internet");
                    sendingMethod = 1;
                    submit.setEnabled(true);
                }
    }

    // Formats the TextView to show in Review Screen
    public void formatReviewInformationTextView(TextView textView){

        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(getContext(), R.style.question);
        } else {
            textView.setTextAppearance(R.style.question);
        }
    }

    public void sendNotificationToZipCode(String zipcode, String key, String message, String type){
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notificationRequests");

        Map notification = new HashMap<>();
        notification.put("zipcode", zipcode);
        notification.put("key", key);
        notification.put("message", message);
        notification.put("type", type);
        Log.d(TAG, "Push notification " + key);
        notificationRef.push().setValue(notification);

    }

    public void setupSendButtonOptions(){

        selectMethodTextView.setText("Method to send Report");

        boolean isInternetAvailable = checkInternetConnection();

        if(!isInternetAvailable){
            selectMethodTextView.setText("No Internet Connection");
        }
    }


    class AddressResultReceiver extends android.os.ResultReceiver {
        public AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            Log.d(TAG, "onReceiveResult");
            if (resultCode == GeoConstant.SUCCESS_RESULT) { //when the thing is done, result is passed back here
                final Address address = resultData.getParcelable(GeoConstant.RESULT_DATA); //this retrieve the address
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {  //this part is where you put whatever you want to do

                        final String locString = address.getLongitude()+"_"+address.getLatitude();
                        final  String country = address.getCountryName();

                        DatabaseReference newChild = db.push();
                        final String key = newChild.getKey();
                        String reportType = incidentReport.getFirstType();

                        Log.d(TAG, "type "+ reportType);

                        IncidentReport smallerSize = Utils.compacitize(incidentReport);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                        String timestamp = simpleDateFormat.format(new Date());
                        final CompactReport compact = new CompactReport(smallerSize, location.getLongitude(),
                                location.getLatitude(), phoneNumber, "Report", timestamp, false, "","", country, "");


                        newChild.setValue(compact, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Dashboard.incidentType = null;
                                Dashboard.incidentReport = new IncidentReport("Bla");
                                saveReportForHistory(compact, key);
                                getActivity().getSupportFragmentManager().
                                        popBackStackImmediate("chooseAction", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                submit.setText("SUBMITTED");
                            }
                        });

                        sendNotificationToZipCode(locString, key, Utils.notificationMessage(compact), reportType);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        ref.child("ReportOwner").child(key).child("owner").
                                setValue(FirebaseInstanceId.getInstance().getToken());

                    }
                });
            }else{
                Log.d(TAG, "Unable to find longitude latitude");
            }
        }
    }

    // To check whether device has an active Internet connection
    public boolean checkInternetConnection(){

        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    // Display Dialog box for the confirmation of the report submission
    public void showSubmitConfirmationDialog(){
        SubmitDialog dialog = new SubmitDialog();
        dialog.showDialog(getActivity(),"Are you ready to submit the report?");
    }

    // Saving reports locally for the History
    public void saveReportForHistory(CompactReport cmpReport, String key){

        CompactReportUtil cmpUtil = new CompactReportUtil();
        Map<String, String> reportData = cmpUtil.parseReportData(cmpReport,"info");

        String uid = key;
        String title = reportData.get("title");
        String information = reportData.get("information");
        double latitude = cmpReport.getLatitude();
        double longitude = cmpReport.getLongitude();
        long unixTime = System.currentTimeMillis() / 1000L;

        Report report = new Report(uid, title, information, latitude, longitude, unixTime);

        DatabaseHandler db = new DatabaseHandler(getContext());
        db.addReport(report);
    }

    /**
     * Displays the given message.
     *
     * @param message The message to show.
     */
    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(getActivity()!=null)
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendNotification(String message){
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("WatchErth")
                        .setContentText(message);
        NotificationManager notificationManager = (NotificationManager) getActivity()
                                                    .getSystemService(getActivity().NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
        Log.d(TAG, "Notify");
    }

    class SubmitDialog {

        public void showDialog(Activity activity, String msg) {

            final Dialog dialog = new Dialog(activity);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_submit_report);

            TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
            text.setText(msg);


            Button dialogNoButton = (Button) dialog.findViewById(R.id.btn_dialog_no);

            Button dialogYesButton = (Button) dialog.findViewById(R.id.btn_dialog_yes);

            dialogNoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogYesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    sendDataOverInternet();
                }
            });

            dialog.show();
        }
    }

    public void sendDataOverInternet(){

        location = Dashboard.location;

        if (location != null) {
            getAddress();
        } else {
            Toast.makeText(getContext(), "Location " + location, Toast.LENGTH_SHORT);
        }
    }

    public String getReportDataAsJSON(){

        location = Dashboard.location;
        IncidentReport smallerSize = Utils.compacitize(incidentReport);

        CompactReport compact = new CompactReport(smallerSize, location.getLongitude(),
                location.getLatitude(), phoneNumber, "Report", null, false, "","", Dashboard.country, "");

        UUID keyUUID = UUID.randomUUID();
        String key = keyUUID.toString();

        saveReportForHistory(compact, key);

        Gson gson = new Gson();
        String data = gson.toJson(compact);

        return data;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }
}


