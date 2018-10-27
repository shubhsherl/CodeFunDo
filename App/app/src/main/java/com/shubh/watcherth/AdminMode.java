package com.shubh.watcherth;

/**
 * Created by NB on 08/07/17.
 * This class is a light-version of InformationListView class that is being re-used to show all the
 * alerts stored in Firebase, very identical to InformationListView. Different:
 *  1. Not getting user's permission.
 *  2. Not getting the distance between the user's and the alert.
 *  3. No search and filter function.
 */


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.adapter.ClickListener;
import com.shubh.watcherth.adapter.InformationRecyclerViewAdapterAdmin;
import com.shubh.watcherth.adapter.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMode extends Fragment {

    private static final String TAG = AdminMode.class.getSimpleName();
    private static final String REPORT = "report";

    private DatabaseReference db;
    List<CompactReport> reportList;
    RecyclerView reportRecyclerView;
    InformationRecyclerViewAdapterAdmin adapter;

    private InformationListView.OnFragmentInteractionListener mListener;

    private Map<CompactReport, String> adminList;

    public AdminMode() {

    }

    public static AdminMode newInstance(String param1, String param2) {
        AdminMode fragment = new AdminMode();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseDatabase.getInstance().getReference().child("Reports");
        adminList = new HashMap<CompactReport, String>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_information_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Chat With Report Senders");
        reportRecyclerView = (RecyclerView) view.findViewById(R.id.informationListView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.reportIncidentFAB);
        fab.setVisibility(View.GONE);

        fetchDataFromFirebase();

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void fetchDataFromFirebase() {

        reportList = new ArrayList<>();
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        reportRecyclerView.setLayoutManager(llm);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    CompactReport cmp = noteDataSnapshot.getValue(CompactReport.class);
                    adminList.put(cmp, noteDataSnapshot.getKey().toString());
                    if(cmp.getType().equals("Report") && !cmp.verified)
                        reportList.add(cmp);
                }

                adapter = new InformationRecyclerViewAdapterAdmin(getContext(), reportList);
                reportRecyclerView.setAdapter(adapter);

                reportRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), reportRecyclerView, new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        CompactReport report = reportList.get(position);
                        Intent intent = new Intent(getContext(), AdminChatThread.class);
                        Log.d(TAG, adminList.get(report));
                        intent.putExtra(REPORT, report);
                        intent.putExtra("key", adminList.get(report));

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
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

}
