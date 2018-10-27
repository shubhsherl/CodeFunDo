package com.shubh.watcherth.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.util.CompactReportUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InformationRecyclerViewAdapter extends RecyclerView.Adapter<InformationRecyclerViewAdapter.InformationViewHolder> {

    Context context;
    List<CompactReport> reportList;
    LatLng currentLocation;
    private final String SPLIT = "~";

    public static class InformationViewHolder extends RecyclerView.ViewHolder {

        TextView reportTitle;
        TextView reportInformation;
        TextView reportLocation;
        TextView reportCountry;
        TextView reportInformationAdditional;
        ImageView incidentTypeLogo;
        ImageView locationMarkerIcon;
        TextView reportDate;
        TextView reportSource;
        ImageView alertLog;

        InformationViewHolder(View itemView) {
            super(itemView);

            reportTitle = (TextView) itemView.findViewById(R.id.reportTitleTextView);
            reportInformation = (TextView) itemView.findViewById(R.id.reportInformationTextView);
            reportLocation = (TextView) itemView.findViewById(R.id.reportLocationTextView);
            reportCountry = (TextView) itemView.findViewById(R.id.reportCountryTextView);
            incidentTypeLogo = (ImageView) itemView.findViewById(R.id.incidentTypeLogo);
            locationMarkerIcon = (ImageView) itemView.findViewById(R.id.locationMarkerIcon);
            reportDate = (TextView) itemView.findViewById(R.id.reportDateTextView);
            reportSource = (TextView) itemView.findViewById(R.id.reportSourceTextView);
            alertLog = (ImageView) itemView.findViewById(R.id.alertSign);
        }
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public InformationRecyclerViewAdapter(){

    }

    public InformationRecyclerViewAdapter(Context context, List<CompactReport> reportList, LatLng location){
        this.context = context;
        this.reportList = reportList;
        this.currentLocation = location;
    }

    void GoToURL(String url){
        Uri uri = Uri.parse(url);
        Intent intent= new Intent(Intent.ACTION_VIEW,uri);
        context.startActivity(intent);
    }

    @Override
    public InformationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_information_list, viewGroup, false);
        InformationViewHolder ivh = new InformationViewHolder(v);
        return ivh;
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    @Override
    public void onBindViewHolder(InformationViewHolder informationViewHolder, final int i) {

        final CompactReport report = reportList.get(i);
        LatLng reportLocation = new LatLng(report.latitude,report.longitude);
        String roundDistance = "";
        String reportTitle = "";

        CompactReportUtil cmpUtil = new CompactReportUtil();
        Map<String, String> reportData = cmpUtil.parseReportData(report,"list");

        //get distance if it is report type
        if(!report.type.equals("feed-missing") && !report.type.equals("feed-weather")){
            double distanceInMile = cmpUtil.distanceBetweenPoints(currentLocation,reportLocation);
            roundDistance = String.format("%.2f", distanceInMile);
            roundDistance = roundDistance + " mi";
           // informationViewHolder.locationMarkerIcon.setVisibility(View.VISIBLE);
        } else {
            // Hiding the location icon as it is not available for feed-missing & feed-weather
          //  informationViewHolder.locationMarkerIcon.setVisibility(View.INVISIBLE);
        }

        if(report.type.equals("Report")){

            boolean verified = Boolean.parseBoolean(reportData.get("isVerified"));
            informationViewHolder.alertLog.setVisibility(View.VISIBLE);

            if (verified == true) {
                informationViewHolder.alertLog.setImageResource(R.drawable.verification_icon);
            } else {
                informationViewHolder.alertLog.setImageResource(R.drawable.not_verfied);
            }
        }

        //binding for different type of report
        if(!report.type.equals("Report")){
            //get title
            reportTitle = reportData.get("title");
            informationViewHolder.reportTitle.setText(reportTitle);

            //get description
            informationViewHolder.reportInformation.setText(takeSubContent(reportData.get("information")));

            //get distance
          //  if(!report.type.equals("feed-missing") && !report.type.equals("feed-weather"))
                informationViewHolder.reportLocation.setText(roundDistance);
          //  else informationViewHolder.reportLocation.setText("");
            //get date
            informationViewHolder.reportDate.setText(reportData.get("date"));

            //get source
            if (report.rf_link.isEmpty())
                informationViewHolder.reportSource.setText("Report");
            else {
                Spanned text = Html.fromHtml("<a href='"+report.rf_link+"'>Source Link</a>");
                informationViewHolder.reportSource.setText(text);
                informationViewHolder.reportSource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GoToURL(report.rf_link);
                    }
                });
            }

        }else {
            //binding for report type
            //get strings of reports
            reportTitle = reportData.get("title");

            //get full information

            String[] fullInfo;
            if (reportData.get("short_des").isEmpty()) {
                fullInfo = reportData.get("information").split(SPLIT);
                informationViewHolder.reportInformation.setText(fullInfo[0].trim());
            } else
                informationViewHolder.reportInformation.setText(reportData.get("short_des"));

            //set title
            informationViewHolder.reportTitle.setText(reportTitle);

            //set distance
            informationViewHolder.reportLocation.setText(roundDistance);

            //set country
            informationViewHolder.reportCountry.setText(reportData.get("country"));

            //set date
            informationViewHolder.reportDate.setText(reportData.get("date"));

            //set source
            if (report.rf_link.isEmpty())
                informationViewHolder.reportSource.setText("Report");
            else {
                Spanned text = Html.fromHtml("<a href='"+report.rf_link+"'>Source Link</a>");
                informationViewHolder.reportSource.setText(text);
                informationViewHolder.reportSource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GoToURL(report.rf_link);
                    }
                });
            }
        }

        //switch (reportData.get("alertLevel")){
           switch (report.alertLevel){
            case "Low":
                informationViewHolder.alertLog.setImageResource(R.drawable.green_alert);
            break;
            case "Medium":
                informationViewHolder.alertLog.setImageResource(R.drawable.orange_alert);
            break;
            case "High":
                informationViewHolder.alertLog.setImageResource(R.drawable.red_alert);
             break;
        }

        switch(reportTitle){
            case "Earthquake":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.earthquake);
                break;
            case "Flood":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.flood);
                break;
            case "Cyclone":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.cyclone);
                break;
            case "Volcano":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.volcano);
                break;
            case "Landslide":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.landslide);
                break;
            case "Weather":
            case "Missing":
            case "Crime":
                informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.rss);
                informationViewHolder.alertLog.setVisibility(View.GONE);
                break;
        }


    }

    public void updateList(List<CompactReport> list){
        reportList.clear();
        reportList.addAll(list);
        notifyDataSetChanged();
    }

    public void filterByQuery(String query){
        List<CompactReport> temp = new ArrayList();

        CompactReportUtil cmpUtil = new CompactReportUtil();

        for(CompactReport cmpReport: reportList){

            Map<String, String> reportData = cmpUtil.parseReportData(cmpReport,"list");
            String reportTitle = reportData.get("title").split(SPLIT)[0];

            if(reportTitle.toLowerCase().equals(query.toLowerCase())){
                temp.add(cmpReport);
            }
        }
        //update recyclerview
       updateList(temp);
    }

    public void filterByCategory(List<String> categoryList){

        List<CompactReport> temp = new ArrayList();
        CompactReportUtil cmpUtil = new CompactReportUtil();

        for(CompactReport cmpReport: reportList){

            Map<String, String> reportData = cmpUtil.parseReportData(cmpReport,"list");
            String reportTitle = reportData.get("title").split(SPLIT)[0];

            for (String category : categoryList) {

                if (reportTitle.toLowerCase().equals(category.toLowerCase())) {
                    temp.add(cmpReport);
                }
            }

        }
        //update recyclerview
        updateList(temp);
    }

    public void filterByDistance(double queryDistance, LatLng specifiedLocation){

        List<CompactReport> temp = new ArrayList();
        CompactReportUtil cmpUtil = new CompactReportUtil();
        double distanceInMile;

        for(CompactReport cmpReport: reportList){
            LatLng reportLocation = new LatLng(cmpReport.latitude,cmpReport.longitude);

            if(specifiedLocation != null){
                distanceInMile = cmpUtil.distanceBetweenPoints(specifiedLocation,reportLocation);
            }else{
                distanceInMile = cmpUtil.distanceBetweenPoints(currentLocation,reportLocation);
            }

            if (distanceInMile <= queryDistance){
                temp.add(cmpReport);
            }
        }
        updateList(temp);
    }

    public void combineFilter(List<String> categoryList, double queryDistance, LatLng specifiedLocation) {

        List<CompactReport> temp = new ArrayList();
        CompactReportUtil cmpUtil = new CompactReportUtil();
        double distanceInMile;

        for (CompactReport cmpReport : reportList) {

            Map<String, String> reportData = cmpUtil.parseReportData(cmpReport,"list");
            String reportTitle = reportData.get("title").split(SPLIT)[0];

            LatLng reportLocation = new LatLng(cmpReport.latitude,cmpReport.longitude);

            if(specifiedLocation != null){
                distanceInMile = cmpUtil.distanceBetweenPoints(specifiedLocation,reportLocation);
            }else{
                distanceInMile = cmpUtil.distanceBetweenPoints(currentLocation,reportLocation);
            }

            for (String category : categoryList) {
                if ((reportTitle.toLowerCase().equals(category.toLowerCase())) && distanceInMile <= queryDistance) {
                    temp.add(cmpReport);
                }
            }
        }

        updateList(temp);
    }

    public String printArr(String[] arr){
        String re = "";
        for(String s : arr){
            re += s +"//";
        }
        return re;
    }

    public String takeSubContent(String s){
        int max = 122;
        int fromIndex = s.length();
        Log.d("InformationAdapter", s.length()+"");
        if(fromIndex > max){
            fromIndex = s.indexOf(" ", max - 10);
            return s.substring(0, fromIndex)+"...more";
        }
        return s;
    }

    public void  addCurrentLoadedData(List<CompactReport> list){
        int initialReportSize = reportList.size();
        reportList.addAll(list);
        notifyItemRangeInserted(initialReportSize,list.size());
    }
}

