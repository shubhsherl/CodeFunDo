package com.shubh.watcherth;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shubh.watcherth.BaseClass.BaseXBeeFragment;
import com.shubh.watcherth.ReportFragments.Constant;
import com.shubh.watcherth.ReportFragments.IncidentType;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.adapter.ClickListener;
import com.shubh.watcherth.adapter.InformationRecyclerViewAdapter;
import com.shubh.watcherth.adapter.RecyclerTouchListener;
import com.shubh.watcherth.db.handler.DatabaseHandler;
import com.shubh.watcherth.db.model.Report;
import com.shubh.watcherth.util.CompactReportUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InformationListView extends BaseXBeeFragment {

    private static final String REPORT = "report";

    private DatabaseReference db;
    List<CompactReport> reportList;
    RecyclerView reportRecyclerView;
    LatLng currentLocation;
    public String phoneNumber;
    private MenuItem mSearchAction;
    private MenuItem mFilterAction;
    private boolean isSearchOpened = false;
    private boolean isFilterApplied = false;
    private EditText searchBar;
    InformationRecyclerViewAdapter adapter;
    CompactReportUtil cmpUtil = new CompactReportUtil();
    final private SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy");

    boolean isDataLoading = false;

    long msTime;

    private FusedLocationProviderClient mFusedLocationClient;

    public static final int PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int PERMISSIONS_REQUEST_PHONE = 0;

    private OnFragmentInteractionListener mListener;

    public InformationListView() {

    }

    public static InformationListView newInstance(String param1, String param2) {
        InformationListView fragment = new InformationListView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        db = FirebaseDatabase.getInstance().getReference().child("Reports");
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return inflater.inflate(R.layout.fragment_information_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("WatchErth");

        reportRecyclerView = (RecyclerView) view.findViewById(R.id.informationListView);
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InformationRecyclerViewAdapter(getContext(), new ArrayList<CompactReport>(), currentLocation);
        reportRecyclerView.setAdapter(adapter);

        checkPermission();

        reportRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

               /* if (!recyclerView.canScrollVertically(1)) {
                    Query loadNewDataQuery = db.orderByChild("utc_timestamp").endAt(msTime).limitToFirst(10);
                    fetchDataFromFirebase(loadNewDataQuery);
                }*/
            }
        });

        FloatingActionButton newReportFab = (FloatingActionButton) view.findViewById(R.id.reportIncidentFAB);
        newReportFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Fragment fragment = Dashboard.incidentType;
                if (fragment == null) {
                    Dashboard.incidentType = new IncidentType();
                    fragment = Dashboard.incidentType;
                }

                FragmentTransaction ft = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrame, fragment)
                        .addToBackStack("chooseAction");

                ft.commit();

            }
        });

        getPhonePermission();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void checkPermission() {

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        adapter.setCurrentLocation(currentLocation);

                        // Query to get reports
                        Query latestReportQuery = db.orderByChild("utc_timestamp").limitToLast(10);
                        fetchDataFromFirebase(latestReportQuery);
                    }
                }
            });

        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

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

    public void fetchDataFromFirebase(Query query) {

        isDataLoading = true;

        reportList = new ArrayList<>();

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    CompactReport cmp = noteDataSnapshot.getValue(CompactReport.class);
                    boolean exist =false;
                    for (int i = 0;i<reportList.size();i++)
                        if (cmp.rf_link.equals(reportList.get(i).rf_link) && cmp.verified) {
                            exist = true;
                            break;
                        }
                    if (!exist) {
                        //reportList.add(cmp);
                        reportList.add(0, cmp);
                        //saveReportForHistory(cmp,noteDataSnapshot.getKey());
                    }
                }

                // msTime = reportList.get(reportList.size()-1).getUtc_timestamp();
                // reportList.remove(reportList.size()-1);

               /* Collections.sort(reportList, new Comparator<CompactReport>() {
                    @Override
                    public int compare(CompactReport o1, CompactReport o2) {
                        Map<String, String> map1 = cmpUtil.parseReportData(o1, "info");
                        Map<String, String> map2 = cmpUtil.parseReportData(o2, "info");*/

                    /*
                    String[] coors1 = map1.get("location").split(",");
                    String[] coors2 = map2.get("location").split(",");

                    Location location1 = new Location("");
                    Location location2 = new Location("");

                    double distanceInMile1 = 0;
                    double distanceInMile2 = 0;

                    if(coors1.length == 2 && coors2.length == 2) {

                        location1.setLongitude(Double.parseDouble(coors1[1]));
                        location1.setLatitude(Double.parseDouble(coors1[0]));


                        location2.setLongitude(Double.parseDouble(coors2[1]));
                        location2.setLatitude(Double.parseDouble(coors2[0]));

                        if(Dashboard.location!=null) {
                            distanceInMile1 = cmpUtil.distanceBetweenPoints(location1, Dashboard.location);
                            distanceInMile2 = cmpUtil.distanceBetweenPoints(location1, Dashboard.location);
                        }
                    }
                       */

                       /* Date date1 = null;
                        Date date2 = null;
                        try {
                            date1 = dateFormat.parse(map1.get("date"));
                            date2 = dateFormat.parse(map2.get("date"));
                        } catch (Exception ex) {
                            Log.d(TAG, "Date format exception");
                        }
                        if(date1==null && date2!=null) return 1;
                        else if(date2==null && date1!=null) return -1;
                        else if(date1==null && date2==null) return 0;

                        if(date2.compareTo(date1) == 0){
                            Location location1 = setUpLocation(map1.get("location"));
                            Location location2 = setUpLocation(map2.get("location"));
                            Double d1 = Double.POSITIVE_INFINITY;
                            Double d2 = Double.POSITIVE_INFINITY;

                            Location curLocation = new Location("");
                            curLocation.setLatitude(currentLocation.latitude);
                            curLocation.setLongitude(currentLocation.longitude);

                            if(location1!=null)
                                d1 = cmpUtil.distanceBetweenPoints(location1, curLocation);
                            if(location2!=null)
                                d2 = cmpUtil.distanceBetweenPoints(location2, curLocation);

                            return -Double.compare(d2, d1);
                        }

                        return date2.compareTo(date1);*/

                    /*
                    if(date1!=null && date2!=null) {
                        if (date1.equals(date2)) {
                            if (distanceInMile1 == distanceInMile2)
                                return -1;
                            return Double.compare(distanceInMile1, distanceInMile2);
                        } else return date1.compareTo(date2);
                    }else return -1;*/
                // }
                //});

                adapter.addCurrentLoadedData(reportList);

                reportRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), reportRecyclerView, new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        CompactReport report = reportList.get(position);
                        Log.d("yo1", report.description + "1");
                        Intent intent = new Intent(getContext(), ViewNotification.class);
                        intent.putExtra(REPORT, report);
                        intent.putExtra("Verified",report.isVerified());
                        intent.putExtra("Description", report.description);
                        intent.putExtra("Country", report.country);
                        intent.putExtra("AlertLevel", report.alertLevel);
                        intent.putExtra("Rf_link", report.rf_link);
                        intent.putExtra("Pop_unit", report.pop_unit);
                        intent.putExtra("Pop", report.population);

                        startActivity(intent);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }
                ));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        isDataLoading = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        checkPermission();
                    } catch (SecurityException e) {
                        Log.e("Watcherth", e.getMessage());
                    }
                }
                return;
            }
            case PERMISSIONS_REQUEST_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //read phone number
                    getPhoneNumberFromDevice();
                    Log.d(TAG, "Phone number" + phoneNumber);
                } else {
                    Log.d(TAG, "Permission denied");
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Phone Permission Required");
                    alertDialog.setMessage("Phone permission is needed to submit a report");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel reporting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                return;
            }
        }

    }

    public void getPhonePermission() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE);
            Log.d(TAG, "Permission requested");
        } else {
            getPhoneNumberFromDevice();
        }
    }

    public void getPhoneNumberFromDevice() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        phoneNumber = sharedPreferences.getString(Constant.PHONE_NUMBER,phoneNumber);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        mFilterAction = menu.findItem(R.id.action_filter);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                handleMenuSearch();
                return true;
            case R.id.action_filter:
                handleMenuFilter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleMenuSearch() {
        ActionBar action = ((AppCompatActivity) getActivity()).getSupportActionBar(); //get the actionbar

        if (isSearchOpened) { //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

            mSearchAction.setIcon(getResources().getDrawable(R.drawable.magnifying_glass));

            Query latestReportQuery = db.orderByChild("utc_timestamp").limitToLast(10);
            fetchDataFromFirebase(latestReportQuery);

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            searchBar = (EditText) action.getCustomView().findViewById(R.id.searchBar); //the text editor

            //this is a listener to do a search when the user clicks on search button
            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

                        String searchQuery = searchBar.getText().toString();
                        adapter.filterByQuery(searchQuery);
                        return true;
                    }
                    return false;
                }
            });


            searchBar.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);

            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.close));

            isSearchOpened = true;
        }
    }

    public void handleMenuFilter() {

        if (isFilterApplied) {
            mSearchAction.setVisible(true);
            mFilterAction.setIcon(getResources().getDrawable(R.drawable.filter));
            isFilterApplied = false;

            Query latestReportQuery = db.orderByChild("utc_timestamp").limitToLast(10);
            fetchDataFromFirebase(latestReportQuery);
        } else {
            Intent intent = new Intent(getContext(), IncidentFilterActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                isFilterApplied = true;

                int distance = data.getIntExtra("distance", 0);
                ArrayList<String> categoryList = data.getStringArrayListExtra("selectedCategory");
                LatLng ll = data.getParcelableExtra("longLat_dataProvider");

                if (categoryList.size() > 0 && distance != 0) {
                    adapter.combineFilter(categoryList, distance, ll);
                } else if (categoryList.size() > 0) {
                    adapter.filterByCategory(categoryList);
                } else {
                    adapter.filterByDistance(distance, ll);
                }

                mSearchAction.setVisible(false);
                mFilterAction.setIcon(getResources().getDrawable(R.drawable.close));
            }
        }

    }

    public Location setUpLocation(String locationCoor) {

        Location location = new Location("");
        String[] coor = locationCoor.split(",");

        location.setLatitude(Double.parseDouble(coor[0]));
        location.setLongitude(Double.parseDouble(coor[1]));

        return location;
    }
}
