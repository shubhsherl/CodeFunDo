package com.shubh.watcherth.ReportObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompactReport implements Parcelable{


    public Map<String, String> rssFeedReport = new HashMap<>();

    public String author;
    public String title;
    public Double longitude;
    public Double latitude;
    public String phoneNumber;
    public String type;
    public String timestamp;
    public boolean verified;
    public boolean isdeclined;
    public long utc_timestamp;
    public String alertLevel = "Low";
    public  String country;
    public boolean got_tweets = false;
    public String short_des;
    public String description = "";
    public String rf_link = "";
    public Double population = 0.00;
    public  String pop_unit;

    public List<String> pathToServer;


    public CompactReport(){}


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isIsdeclined() {
        return isdeclined;
    }

    public void setIsdeclined(boolean isdeclined) {
        this.isdeclined = isdeclined;
    }

    public List<String> getPathToServer() {
        return pathToServer;
    }

    public void setPathToServer(List<String> pathToServer) {
        this.pathToServer = pathToServer;
    }

    public Map<String, String> getRssFeedReport() {
        return rssFeedReport;
    }

    public long getUtc_timestamp() {
        return utc_timestamp;
    }

    public void setUtc_timestamp(long utc_timestamp) {
        this.utc_timestamp = utc_timestamp;
    }


    public CompactReport(IncidentReport incidentReport, Double longitude, Double latitude,
                         String phone, String type, String timestamp, boolean isVerified, String short_des, String description, String country, String rf_link){

        for(Report r : incidentReport.reports){
            Log.d("CompactReport", r.toString());

            ArrayList<String> questions = new ArrayList<>();

            for(Question q : r.questions){
                Log.d("CompactReport" + r.type, q.getQuestion()+", "+q.getChoices());
                questions.add(q.getQuestion()+":"+q.getChoices());
                if (q.question == "Number of people affected") {
                    if (q.getChoices().contains("More")) {
                        this.population = 100.00;
                        this.pop_unit = "+";
                    } else if (q.getChoices().contains("1-10")) {
                        this.population = 10.00;
                        this.pop_unit = " >";
                    } else {
                        this.population = 100.00;
                        this.pop_unit = " >";
                    }
                }else
                    this.alertLevel = q.getChoices();
            }
        }

        if(incidentReport.reports.size() == 0) Log.d("CompactReport", "Empty");

        this.title = incidentReport.getFirstType();
        this.longitude = longitude;
        this.latitude = latitude;
        this.phoneNumber = phone;
        this.type = type;
        this.timestamp = timestamp;
        this.verified = isVerified;
        this.country = country;
        if (description.isEmpty())
            this.description = "Number of People affected "+this.population +" "+ this.pop_unit +"\n" + "Alert Level = " + this .alertLevel;
        else
            this.description = description;
        this.rf_link = rf_link;
        if (short_des.isEmpty())
            this.short_des = "Number of People affected "+this.population +" "+ this.pop_unit +"\n" + "Alert Level = " + this .alertLevel;
        else
            this.short_des = short_des;
    }

    //parceble implementation
    //Parceble implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(phoneNumber);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(type);
        dest.writeString(timestamp);
        dest.writeString(title);
        dest.writeString(author);
        /*dest.writeString(description);
        dest.writeString(alertLevel);
        dest.writeString(short_des);
        dest.writeString(rf_link);
        dest.writeString(pop_unit);
        dest.writeDouble(population);
        dest.writeString(country);*/

    }

    public static final Parcelable.Creator<CompactReport> CREATOR
            = new Parcelable.Creator<CompactReport>() {
        public CompactReport createFromParcel(Parcel in) {
            return new CompactReport(in);
        }

        public CompactReport[] newArray(int size) {
            return new CompactReport[size];
        }
    };

    private CompactReport(Parcel in) {

        phoneNumber = in.readString();
        /*country = in.readString();
        population = in.readDouble();
        pop_unit =in.readString();
        rf_link = in.readString();
        alertLevel = in.readString();
        description = in.readString();*/
        longitude = in.readDouble();
        latitude = in.readDouble();
        type = in.readString();
        timestamp = in.readString();
        title = in.readString();
        author = in.readString();

    }
}
