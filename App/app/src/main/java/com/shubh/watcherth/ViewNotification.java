package com.shubh.watcherth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.shubh.watcherth.BaseClass.BaseXbeeActivity;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.util.CompactReportUtil;

import org.w3c.dom.Text;

import java.util.Map;


public class ViewNotification extends BaseXbeeActivity implements OnMapReadyCallback {
    public final String TAG = "ViewNotification";
    public final String REPORT = "report";
    public String reportKey;
    private CompactReport report;
    private FirebaseDatabase db;
    private CompactReportUtil cmpUtils;
    private final String SPLIT = "~";

    private TextView titleTextView;
    private TextView informationTextView;
    private ImageView incidentTypeLogo;
    private TextView dateTextView;
    private ImageView alertLevel;
    private TextView distanceTextView;
    private TextView sourceTextView;
    private TextView popTextView;
    private TextView doTextView;
    private TextView donTextView;
    private TextView countryNumber;

    private double longitude;
    private double latitude;
    private SupportMapFragment mapFragment;
    LatLng mapLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseDatabase.getInstance();

        cmpUtils = new CompactReportUtil();

        //get views
        titleTextView = (TextView) findViewById(R.id.viewNotificationReportTitle);
        informationTextView = (TextView) findViewById(R.id.viewNotificationReportInformation);
        incidentTypeLogo = (ImageView) findViewById(R.id.viewNotificationTypeLogo);
        dateTextView = (TextView) findViewById(R.id.viewNotificationDateTextView);
        sourceTextView = (TextView) findViewById(R.id.viewNotificationSourceTextView);
        distanceTextView = (TextView) findViewById(R.id.viewNotificationLocationTextView);
        popTextView = (TextView) findViewById(R.id.viewNotificationPop);
        doTextView = (TextView) findViewById(R.id.viewNotificationDo);
        donTextView = (TextView) findViewById(R.id.viewNotificationDon);
        countryNumber = (TextView) findViewById(R.id.viewNotificationcontactcall);

        alertLevel = (ImageView) findViewById(R.id.alertSign);

        if (getIntent().hasExtra(REPORT)) {
            //case when ViewNotification was triggered by an intent from another activity
            report = getIntent().getParcelableExtra(REPORT);
            report.description = getIntent().getStringExtra("Description");
            report.verified = getIntent().getBooleanExtra("Verified", false);
            report.alertLevel = getIntent().getStringExtra("AlertLevel");
            report.rf_link = getIntent().getStringExtra("Rf_link");
            report.country = getIntent().getStringExtra("Country");
            report.pop_unit = getIntent().getStringExtra("Pop_unit");
            report.population = getIntent().getDoubleExtra("Pop", 0.00);
            setUpReportDetail(report);
        } else {
            //case ViewNotification was triggered by clickAction when user taps on notification
            if (getIntent() != null && getIntent().getExtras() != null) {
                reportKey = getIntent().getExtras().getString("key");
            }
            if (reportKey != null) {
                Query notifiedReportQuery = db.getReference().child("Reports").orderByKey().equalTo(reportKey);

                notifiedReportQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        report = dataSnapshot.getValue(CompactReport.class);
                        setUpReportDetail(report);
                        Log.d(TAG, "onChildAdded: " + report.description + "2");
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    void GoToURL(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void setUpReportDetail(final CompactReport report) {
        CompactReportUtil cmpUtil = new CompactReportUtil();

        Map<String, String> map = cmpUtil.parseReportData(report, "info");

        String location = map.get("location");
        String title = "";

        //set date
        String date = map.get("date");
        dateTextView.setText(date);

        //set distance
        if (!report.type.equals("feed-weather") && !report.type.equals("feed-missing")) {
            //get distance
            latitude = Double.parseDouble(location.split(",")[0]);
            longitude = Double.parseDouble(location.split(",")[1]);

            Location location1 = new Location("");

            location1.setLongitude(longitude);
            location1.setLatitude(latitude);

            double distanceInMile = cmpUtil.distanceBetweenPoints(location1, Dashboard.location);

            String roundDistance = String.format("%.2f", distanceInMile);
            roundDistance = roundDistance + " mi";

            distanceTextView.setText(roundDistance);
            String popAffected = report.population + " " + report.pop_unit;
            popTextView.setText(popAffected);

            //set up map
            mapLocation = new LatLng(latitude, longitude);
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.viewReportDetail_mapfragment);
            mapFragment.getMapAsync(this);
        }

        if (!report.type.equals("Report")) {

            title = map.get("title");
            String information = map.get("information");

            Log.i("CRIME", information);

            String source = map.get("author");

            titleTextView.setText(title);
            informationTextView.setText(information);
            sourceTextView.setText(source);
        } else {
            //case report is user's report
            String[] titles = map.get("title").split(SPLIT);
            String[] informations = map.get("information").split(SPLIT);
            title = titles[0];

            titleTextView.setText(titles[0]);
            informationTextView.setText(report.description);
            if (map.get("link") == null)
                sourceTextView.setText("Report");
            else {
                Spanned text = Html.fromHtml("<a href='" + report.rf_link + "'>Source Link</a>");
                sourceTextView.setText(text);
                sourceTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GoToURL(report.rf_link);
                    }
                });
            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.viewNotificationAllInfo);

            for (int i = 1; i < titles.length; i++) {
                TextView extraTitle = new TextView(this);
                TextView extraDetail = new TextView(this);

                setTextViewAttribute(extraTitle, true);
                setTextViewAttribute(extraDetail, false);

                extraTitle.setText(titles[i]);
                extraDetail.setText(informations[i]);

                layout.addView(extraTitle);
                layout.addView(extraDetail);
            }
        }

        setTitle("Incident Information");

        switch (report.alertLevel) {
            case "High":
                alertLevel.setImageResource(R.drawable.red_alert);
                break;
            case "Medium":
                alertLevel.setImageResource(R.drawable.orange_alert);
                break;
            case "Low":
                alertLevel.setImageResource(R.drawable.green_alert);
                break;
        }
        if (report.country != null) {
            switch (report.country) {
                case "India":
                    countryNumber.setText(R.string.India_emergency_contact);
                    break;
                case "Mexico":
                    countryNumber.setText(R.string.mexico_emergency_contact);
                    break;
                case "China":
                    countryNumber.setText(R.string.china_emergency_contact);
                    break;
                case "USA":
                case "America":
                    countryNumber.setText(R.string.usa_emergency_contact);
                    break;
                default:
                    countryNumber.setText("");
            }
            if (!countryNumber.getText().toString().isEmpty()) {
                countryNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse(countryNumber.getText().toString()));
                        String requiredPermission = "android.permission.CALL_PHONE";
                        if (getBaseContext().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {
                            startActivity(callIntent);
                        } else Log.d(TAG, "onClick: loco");
                    }
                });
            }
        }
        switch (title) {
            case "Earthquake":
                incidentTypeLogo.setImageResource(R.drawable.earthquake);
                doTextView.setText(R.string.earthquakedo);
                donTextView.setText(R.string.earthquakedon);
                break;
            case "Cyclone":
                incidentTypeLogo.setImageResource(R.drawable.cyclone);
                doTextView.setText(R.string.cyclonedo);
                donTextView.setText(R.string.cyclonedon);
                break;
            case "Flood":
                incidentTypeLogo.setImageResource(R.drawable.flood);
                doTextView.setText(R.string.flooddo);
                donTextView.setText(R.string.flooddon);
                break;
            case "Volcano":
                incidentTypeLogo.setImageResource(R.drawable.volcano);
                doTextView.setText(R.string.volcanodo);
                donTextView.setText(R.string.volcanodon);
                break;
            case "Landslide":
                incidentTypeLogo.setImageResource(R.drawable.landslide);
                doTextView.setText(R.string.landslidedo);
                donTextView.setText(R.string.landslidedon);
                break;
            case "Weather":
            case "Missing":
            case "Crime":
                incidentTypeLogo.setImageResource(R.drawable.rss);
                break;
        }
    }


    public void setLocation(Double lat, Double longitude) {
        mapLocation = new LatLng(lat, longitude);
    }

    public void setTextViewAttribute(TextView textView, boolean title) {
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#31343a"));

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int dpValue = 8;
        int px = dpValue * metrics.densityDpi / 160;
        textView.setPadding(px, 0, 0, 0);

        if (title) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, mapLocation.toString());
        googleMap.addMarker(new MarkerOptions().position(mapLocation).title("Marker in Location"));
        if (report.verified) {
            googleMap.addMarker(new MarkerOptions().
                    position(new LatLng(mapLocation.latitude + 3.21667, mapLocation.longitude + 2.0)).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).
                    title("Safe Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, 5));
        } else
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, 16));
    }
}
