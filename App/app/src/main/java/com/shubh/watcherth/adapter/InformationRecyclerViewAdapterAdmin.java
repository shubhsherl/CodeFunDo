package com.shubh.watcherth.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.shubh.watcherth.Dashboard;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.util.CompactReportUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by NB on 8/12/2017.
 *
 * This is a duplicate of InformationRecyclerViewAdapter but with 1 lesser field to input.
 * This one is being used for the AdminMode class. This one can be deleted if we can:
 *  1: Merge this with the InformationRecyclerViewAdapter.
 *  2: Change the AdminMode class so that the admin class will use the InformationRecyclerViewAdapter
 *  instead of this one.
 *
 */
public class InformationRecyclerViewAdapterAdmin extends RecyclerView.Adapter<InformationRecyclerViewAdapterAdmin.InformationViewHolder> {

    Context context;
    List<CompactReport> reportList;
    LatLng currentLocation;

    public static class InformationViewHolder extends RecyclerView.ViewHolder {

        TextView reportTitle;
        TextView reportInformation;
        TextView reportLocation;
        ImageView incidentTypeLogo;

        InformationViewHolder(View itemView) {
            super(itemView);

            reportTitle = (TextView) itemView.findViewById(R.id.reportTitleTextView);
            reportInformation = (TextView) itemView.findViewById(R.id.reportInformationTextView);
            reportLocation = (TextView) itemView.findViewById(R.id.reportLocationTextView);
            incidentTypeLogo = (ImageView) itemView.findViewById(R.id.incidentTypeLogo);

        }
    }

    public InformationRecyclerViewAdapterAdmin(){

    }

    public InformationRecyclerViewAdapterAdmin(Context context, List<CompactReport> reportList){
        this.context = context;
        this.reportList = reportList;
        this.currentLocation = new LatLng(Dashboard.location.getLatitude(),Dashboard.location.getLongitude());
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

        CompactReport report = reportList.get(i);
        LatLng reportLocation = new LatLng(report.latitude, report.longitude);
        String roundDistance = "";

        CompactReportUtil cmpUtil = new CompactReportUtil();
        Map<String, String> reportData = cmpUtil.parseReportData(report, "info");

        if (report.getType().equals("Report")) {

            double distanceInMile = cmpUtil.distanceBetweenPoints(currentLocation, reportLocation);
            roundDistance = String.format("%.2f", distanceInMile);
            roundDistance = roundDistance + " miles";

            String[] reportTitles = reportData.get("title").split("~");
            String reportTitle = reportTitles[0];

            String[] fullInfoArray = reportData.get("information").split("~");
            String fullInfo = fullInfoArray[0].trim();

            informationViewHolder.reportTitle.setText(reportTitle);
            informationViewHolder.reportInformation.setText(fullInfo);
            if (currentLocation != null) {
                informationViewHolder.reportLocation.setText(roundDistance);
            }
            switch (reportTitle) {
                case "Earthquake":
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.earthquake);
                    break;
                case "Flood":
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.flood);
                    break;
                case "Volcano":
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.volcano);
                    break;
                case "Cyclone":
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.cyclone);
                    break;
                case "Landslide":
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.landslide);
                    break;
                default:
                    informationViewHolder.incidentTypeLogo.setImageResource(R.drawable.rss);
                    break;
            }
        }
    }
}
