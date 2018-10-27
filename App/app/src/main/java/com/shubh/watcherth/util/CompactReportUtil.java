
package com.shubh.watcherth.util;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.shubh.watcherth.ReportObject.CompactReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CompactReportUtil {

    private final String SPLIT = "~";

    public Map<String,String> parseReportData(CompactReport report, String source){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        SimpleDateFormat dateFormatAPI = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        SimpleDateFormat displayDate = new SimpleDateFormat("E, dd MMM yyyy");
        SimpleDateFormat missingKidDate = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss 'EDT'");

        Date date = null;
        String formattedDate = "";

        try{
            String timestamp = report.getTimestamp();
            if(timestamp != null)
                date = dateFormatAPI.parse(timestamp);
                formattedDate = displayDate.format(date);
        }catch (Exception e){

        }

        if(report.type.equals("Report")) {
            String reportTitle = report.title;
            String individualInformation = "";
            String fullInformation = "";
            String location = "";

            Map<String, String> reportData = new HashMap<String, String>();


            location = String.valueOf(report.latitude) + "," + String.valueOf(report.longitude);

            reportData.put("title", reportTitle);
            reportData.put("information", fullInformation);
            reportData.put("location", location);
            reportData.put("isVerified",String.valueOf(report.isVerified()));
            reportData.put("date", formattedDate);
            reportData.put("country", report.country);
            reportData.put("link", report.rf_link);
            reportData.put("alertLevel",report.alertLevel);
            reportData.put("short_des",report.short_des);

            return reportData;
        }else if(report.type.equals("feed-crime")){

            String reportTitle = "Crime";
            String info = "";
            String location = "";
            String author="";

            Map<String, String> compactReports = report.getRssFeedReport();
            Map<String, String> reportData = new HashMap<>();

            info = compactReports.get("summary") + "\n" + report.getTitle();

            location = String.valueOf(report.latitude) + "," + String.valueOf(report.longitude);
            author = report.getAuthor();

            reportData.put("title", reportTitle);
            reportData.put("information", info);
            reportData.put("location", location);
            reportData.put("isVerified","false");
            reportData.put("date",formattedDate);
            reportData.put("author", author);
            reportData.put("country", report.country);
            reportData.put("link", report.rf_link);
            reportData.put("alertLevel",report.alertLevel);
            reportData.put("short_des",report.short_des);

            return reportData;
        }else if(report.type.equals("feed-weather")){

            String reportTitle = "Weather";
            String info = "";
            String location = "";

            Map<String, String> reportData = new HashMap<String, String>();



            reportData.put("title", reportTitle);
            reportData.put("information", info);
            reportData.put("location", location);
            reportData.put("isVerified","false");
            reportData.put("country", report.country);
            reportData.put("link", report.rf_link);
            reportData.put("alertLevel",report.alertLevel);
            reportData.put("short_des",report.short_des);
            return reportData;
        }else{
            String reportTitle = "Missing";
            String info = "";
            String location = "";

            Map<String, String> compactReports = report.getRssFeedReport();
            Map<String, String> reportData = new HashMap<String, String>();

            try{
                //Date reportDate = missingKidDate.parse(compactReports.get("date").get(0));
                //date = displayDate.format(reportDate);
            }catch (Exception ex){

            }

            switch (source){
                case "list":
                    info = parseMissingPersonInfoForListView(compactReports);
                    break;
                case "info":
                    info = parseMissingPersonInfoForInforamtionView(compactReports);
                    break;
            }

            reportData.put("title", reportTitle);
            reportData.put("information", info);
            reportData.put("location", "");
            reportData.put("isVerified","false");
            reportData.put("author", "missingkids.com");
            reportData.put("date", formattedDate);
            reportData.put("country", report.country);
            reportData.put("link", report.rf_link);
            reportData.put("alertLevel",report.alertLevel);
            reportData.put("short_des",report.short_des);
            return reportData;
        }
    }

    public double distanceBetweenPoints(LatLng startPoint, LatLng endPoint){

        float[] results = new float[1];

        Location.distanceBetween(startPoint.latitude,startPoint.longitude,
                endPoint.latitude,endPoint.longitude,results);

        double distanceInMile = results[0] * 0.000621371192;

        return distanceInMile;

    }

    public double distanceBetweenPoints(Location startPoint, Location endPoint){

        float[] results = new float[1];

        Location.distanceBetween(startPoint.getLatitude(),startPoint.getLongitude(),
                endPoint.getLatitude(),endPoint.getLongitude(),results);

        double distanceInMile = results[0] * 0.000621371192;

        return distanceInMile;

    }


    public String parseWeatherInformationForListView(Map<String, ArrayList<String>> compactReports){

        StringBuffer info = new StringBuffer();

        String title = compactReports.get("title").get(0);
        String summary = compactReports.get("summary").get(0);

        summary = summary.replace(title + " ","");

        info.append(title);
        info.append("\n");
        info.append(summary);
        return info.toString();

    }

    public String parseWeatherInformationForInformationView(Map<String, ArrayList<String>> compactReports){

        StringBuffer info = new StringBuffer();

        String title = compactReports.get("title").get(0);
        String summary = compactReports.get("summary").get(0);
        String severity = compactReports.get("severity").get(0);
        String area = compactReports.get("area").get(0);
        String effectiveDate = compactReports.get("effectiveDate").get(0);
        String expiryDate = compactReports.get("expireDate").get(0);

        summary = summary.replace(title + " ","");

        info.append(title);
        info.append("\n");
        info.append(summary);
        info.append("\n");
        info.append("Severity: " + severity);
        info.append("\n");
        info.append("Area Affected: " + area);
        info.append("\n");
        info.append("Effective Date: " + effectiveDate );
        info.append("\n");
        info.append("Expiry Date: "+ expiryDate);

        return info.toString();
    }

    public String parseMissingPersonInfoForListView(Map<String, String> compactReports){

        StringBuffer info = new StringBuffer();

        String summary = compactReports.get("summary");
        String date = compactReports.get("date");

        info.append(summary);
        info.append("\n");
        info.append(date);

        return info.toString();

    }

    public String parseMissingPersonInfoForInforamtionView(Map<String, String> compactReports){

        StringBuffer info = new StringBuffer();

        String title = compactReports.get("title");
        String summary = compactReports.get("summary");
        String date = compactReports.get("date");

        info.append(title);
        info.append("\n");
        info.append(summary);
        info.append("\n");
        info.append(date);

        return info.toString();

    }
}
