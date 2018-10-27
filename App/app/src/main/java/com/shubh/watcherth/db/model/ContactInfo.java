package com.shubh.watcherth.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class ContactInfo implements Parcelable {

    // This is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ContactInfo> CREATOR = new Parcelable.Creator<ContactInfo>() {
        public ContactInfo createFromParcel(Parcel in) {
            return new ContactInfo(in);
        }

        public ContactInfo[] newArray(int size) {
            return new ContactInfo[size];
        }
    };
    public String UserContact;
    public String[] EmergencyContact;

    public ContactInfo() {

    }

    public ContactInfo(String userContact, String[] emergencyContact) {
        this.UserContact = userContact;
        this.EmergencyContact = emergencyContact;
    }


    private ContactInfo(Parcel in) {
        UserContact = in.readString();
        EmergencyContact = in.createStringArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {

        out.writeString(UserContact);
        out.writeStringArray(EmergencyContact);
    }

}

