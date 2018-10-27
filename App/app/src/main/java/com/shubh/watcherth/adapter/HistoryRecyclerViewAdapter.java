package com.shubh.watcherth.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.shubh.watcherth.ClientChatThread;
import com.shubh.watcherth.R;
import com.shubh.watcherth.db.model.Report;
import com.shubh.watcherth.util.CompactReportUtil;

import java.util.List;

/**
 * Created by Purvesh on 7/27/2017.
 */

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.InformationViewHolder> {

    Context context;
    List<Report> reportList;
    LatLng currentLocation;
    String roundDistance;
    String SPLIT="~";

    private Intent clientChatThread;

    public static class InformationViewHolder extends RecyclerView.ViewHolder {

        TextView reportTitle;
        TextView reportInformation;
        TextView reportLocation;
        ImageView incidentTypeLogo;
        TextView reportDate;
        TextView reportSource;

        private ClickListener mClickListener;

        InformationViewHolder(View itemView) {
            super(itemView);

            reportTitle = (TextView) itemView.findViewById(R.id.reportTitleTextView);
            reportInformation = (TextView) itemView.findViewById(R.id.reportInformationTextView);
            reportLocation = (TextView) itemView.findViewById(R.id.reportLocationTextView);
            incidentTypeLogo = (ImageView) itemView.findViewById(R.id.incidentTypeLogo);
            reportDate = (TextView) itemView.findViewById(R.id.reportDateTextView);
            reportSource = (TextView) itemView.findViewById(R.id.reportDateTextView);
            reportSource.setVisibility(View.GONE);
            reportDate.setVisibility(View.GONE);

            // Set ClickListener for the entire row, can set on individual components within a row
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }

        public void setOnClickListener(ClickListener clickListener) {
            mClickListener = clickListener;
        }

        //Interface to send callbacks...
        public interface ClickListener {
            void onItemClick(View view, int position);

            void onItemLongClick(View view, int position);
        }
    }

    public HistoryRecyclerViewAdapter(Context context, List<Report> reportList, LatLng location){
        this.context = context;
        this.reportList = reportList;
        this.currentLocation = location;
    }

    @Override
    public InformationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_information_list, parent, false);
        HistoryRecyclerViewAdapter.InformationViewHolder ivh = new HistoryRecyclerViewAdapter.InformationViewHolder(v);
        ivh.setOnClickListener(new InformationViewHolder.ClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                switchActivity(v, pos);
            }

            @Override
            public void onItemLongClick(View v, int pos) {
                switchActivity(v, pos);
            }
        });
        return ivh;
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    @Override
    public void onBindViewHolder(InformationViewHolder holder, int position) {

        Report report = reportList.get(position);

        String title = report.getTitle().split(SPLIT)[0];
        String information = report.getInformation().split(SPLIT)[0];
        LatLng incidentLocation = new LatLng(report.getLatitude(),report.getLongitude());

        CompactReportUtil cmpUtil = new CompactReportUtil();
        double distanceInMile = cmpUtil.distanceBetweenPoints(currentLocation,incidentLocation);
        roundDistance = String.format("%.2f", distanceInMile);
        roundDistance = roundDistance + " miles far";

        holder.reportTitle.setText(title);
        holder.reportInformation.setText(information);
        holder.reportLocation.setText(roundDistance);

        switch(title){
            case "Earthquake":
                holder.incidentTypeLogo.setImageResource(R.drawable.earthquake);
                break;
            case "Flood":
                holder.incidentTypeLogo.setImageResource(R.drawable.flood);
                break;
            case "Cyclone":
                holder.incidentTypeLogo.setImageResource(R.drawable.cyclone);
                break;
            case "Landslide":
                holder.incidentTypeLogo.setImageResource(R.drawable.landslide);
                break;
            case "Volcano":
                holder.incidentTypeLogo.setImageResource(R.drawable.volcano);
                break;
        }
    }

    private void switchActivity(View v, int pos){
        Report report = reportList.get(pos);
        Context context = v.getContext();
        clientChatThread = new Intent(context, ClientChatThread.class);
        clientChatThread.putExtra("reportObj", report);
        clientChatThread.putExtra("roundDistance", roundDistance);
        context.startActivity(clientChatThread);
    }
}