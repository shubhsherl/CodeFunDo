package com.shubh.watcherth.ReportObject;

import android.util.Log;

import java.util.ArrayList;


public class Utils {

    public static Report buildEmergencyReport() {
        Report emergency = new Report("Emergency", new ArrayList<Question>());
        Question q = new Question();
        q.question = "Emergency";
        emergency.addReportItem(q);
        return emergency;
    }

    public static Report buildCycloneReport() {
        Report cyclone = new Report("Cyclone", new ArrayList<Question>());

        Question q1 = new Question();
        q1.question = "Number of people affected";
        Question q2 = new Question();
        q2.question = "Crisis Alert Level";

        cyclone.addReportItem(q1);
        cyclone.addReportItem(q2);
        return cyclone;
    }

    public static Report buildFloodReport() {
        Report flood = new Report("Flood", new ArrayList<Question>());

        Question q1 = new Question();
        q1.question = "Number of people affected";
        Question q2 = new Question();
        q2.question = "Crisis Alert Level";

        flood.addReportItem(q1);
        flood.addReportItem(q2);

        return flood;
    }

    public static Report buildVolcanoReport() {
        Report volcano = new Report("Volcano", new ArrayList<Question>());

        Question q1 = new Question();
        q1.question = "Number of people affected";
        Question q2 = new Question();
        q2.question = "Crisis Alert Level";

        volcano.addReportItem(q1);
        volcano.addReportItem(q2);

        return volcano;
    }

    public static Report buildLandslideReport() {
        Report landslide = new Report("Landslide", new ArrayList<Question>());
        Question q1 = new Question();
        q1.question = "Number of people affected";
        Question q2 = new Question();
        q2.question = "Crisis Alert Level";

        landslide.addReportItem(q1);
        landslide.addReportItem(q2);

        return landslide;
    }

    public static Report buildEarthquakeReport() {
        Report earthquake = new Report("Earthquake", new ArrayList<Question>());
        Question q1 = new Question();
        q1.question = "Number of people affected";
        Question q2 = new Question();
        q2.question = "Crisis Alert Level";

        earthquake.addReportItem(q1);
        earthquake.addReportItem(q2);

        return earthquake;
    }

    public static boolean hasReport(IncidentReport incidentReport, int type) {
        Report report = incidentReport.getReport(type);
        return !report.isEmpty();
    }

    public static IncidentReport compacitize(IncidentReport incidentReport) {
        ArrayList<Report> r = incidentReport.reports;
        IncidentReport report = new IncidentReport();
        report.reports = new ArrayList<Report>();
        for (int i = 0; i < r.size(); i++) {
            if (!r.get(i).isEmpty()) {
                Log.d("UTILS", "Before Strip " + r.toString());
                //remove question without answer
                stripQuestion(r.get(i));
                Log.d("UTILS", "After Strip" + r.toString());
                report.reports.add(r.get(i));
            } else {
                Log.d("UTILS", r.toString() + "is empty");
            }
        }
        Log.d("UTILS", "End with" + r.toString());
        Log.d("UTILS", "Report is " + report.toString());
        return report;
    }

    public static void stripQuestion(Report report) {
        ArrayList<Integer> index = new ArrayList<>();
        for (int i = 0; i < report.questions.size(); i++) {
            if (report.questions.get(i).empty())
                index.add(i);
        }

        for (int i = 0; i < index.size(); i++) {
            report.questions.remove(index.get(i));
        }
    }

    public static String notificationMessage(CompactReport report) {
        String s = "Report of ";
        s += report.title + " in " + report.country;
        return s;
    }

}