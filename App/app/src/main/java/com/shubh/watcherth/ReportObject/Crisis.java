package com.shubh.watcherth.ReportObject;

import java.util.ArrayList;

public class Crisis {
    public ArrayList<String> gn_parentCountry;
    public String _id, crisis_alertLevel, crisis_eventid, dc_date, dc_description, dc_subject, dc_title, rdfs_seeAlso, severity_unit, pop_unit;

    //dc_title = short description  and dc_subject(0) = type of crises

    public Double population, severity;

    public String uid;

    public Double latitude;
    public Double longitude;
    public long timestamp;



}
