package com.shubh.watcherth.db.handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.vision.barcode.Barcode;
import com.shubh.watcherth.db.model.ContactInfo;
import com.shubh.watcherth.db.model.Report;

import java.util.ArrayList;
import java.util.List;


public class DatabaseContacts extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "watcherth";

    private static final String TABLE_CONTACTS = "contacts";
    private static final String KEY_USER_CONTACT = "user";
    private static final String KEY_EMERGENCY_1 = "emergency1";
    private static final String KEY_EMERGENCY_2 = "emergency2";
    private static final String KEY_EMERGENCY_3 = "emergency3";
    private static final String KEY_EMERGENCY_4 = "emergency4";


    public DatabaseContacts(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_REPORTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_USER_CONTACT + " TEXT,"
                + KEY_EMERGENCY_1 + " TEXT,"
                + KEY_EMERGENCY_2 + " TEXT,"
                + KEY_EMERGENCY_3 + " TEXT,"
                + KEY_EMERGENCY_4 + " TEXT)";
        db.execSQL(CREATE_REPORTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);

    }

    // code to add the new report
    public void addContact(ContactInfo contact) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_CONTACT, contact.UserContact);
        values.put(KEY_EMERGENCY_1, contact.EmergencyContact[0]);
        values.put(KEY_EMERGENCY_2, contact.EmergencyContact[1]);
        values.put(KEY_EMERGENCY_3, contact.EmergencyContact[2]);
        values.put(KEY_EMERGENCY_4, contact.EmergencyContact[3]);



        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get all reports in a list view
    public ContactInfo getContact() {

        ContactInfo contactInfo = new ContactInfo();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contactInfo.UserContact = cursor.getString(0);
                contactInfo.EmergencyContact[0] = cursor.getString(1);
                contactInfo.EmergencyContact[1] = cursor.getString(2);
                contactInfo.EmergencyContact[2] = cursor.getString(3);
                contactInfo.EmergencyContact[3] = cursor.getString(4);

            } while (cursor.moveToNext());
        }

        // return contact list
        return contactInfo;
    }
}