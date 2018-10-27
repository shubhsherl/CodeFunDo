package com.shubh.watcherth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.shubh.watcherth.LocationUtils.GeoConstant;
import com.shubh.watcherth.LocationUtils.GeocodeIntentService;
import com.shubh.watcherth.ReportFragments.Constant;
import com.shubh.watcherth.ReportObject.Subscription;

import java.util.concurrent.TimeUnit;

public class Setting extends AppCompatActivity {


    private CheckBox categoryEmergency;
    private CheckBox categoryCyclone;
    private CheckBox categoryFlood;
    private CheckBox categoryVolcano;
    private CheckBox categoryLandslide;
    private CheckBox categoryEarthquake;
    private EditText addressTextField;
    private EditText userContact;
    private EditText[] emergencyContact = new EditText[4];
    private Spinner distanceSpinner;
    private String subscribeAddress;
    private String subscribeDistance;
    private Button reset;
    private String TAG = Setting.class.getSimpleName();
    private AddressResultReceiver resultReceiver;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        setTitle("Settings");

        categoryEmergency = (CheckBox) findViewById(R.id.checkbox_filter_emergency);
        categoryCyclone = (CheckBox) findViewById(R.id.checkbox_filter_cyclone);
        categoryFlood = (CheckBox) findViewById(R.id.checkbox_filter_flood);
        categoryVolcano = (CheckBox) findViewById(R.id.checkbox_filter_volcano);
        categoryLandslide = (CheckBox) findViewById(R.id.checkbox_filter_Landslide);
        categoryEarthquake = (CheckBox) findViewById(R.id.checkbox_filter_earthquake);
        userContact = (EditText) findViewById(R.id.userNumber);
        emergencyContact[0] = (EditText) findViewById(R.id.emergencyNumber1);
        emergencyContact[1] = (EditText) findViewById(R.id.emergencyNumber2);
        emergencyContact[2] = (EditText) findViewById(R.id.emergencyNumber3);
        emergencyContact[3] = (EditText) findViewById(R.id.emergencyNumber4);

        readSharedPreference();

        addressTextField = (EditText) findViewById(R.id.subscription_setting_address);

        //set up distance spinner
        distanceSpinner = (Spinner) findViewById(R.id.subscription_setting_distance_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.subscription_setting_distance, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        distanceSpinner.setAdapter(adapter);
        subscribeDistance = (String) adapter.getItem(0);


        reset = (Button) findViewById(R.id.reset_notification);

        //address translation
        resultReceiver = new AddressResultReceiver(null);
    }

    //Spinner handler
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        subscribeDistance = (String) parent.getItemAtPosition(pos);
        Log.d(TAG, subscribeDistance);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        subscribeDistance = (String) parent.getItemAtPosition(0);
    }
    //Spinner hanlder

    public void getCoordinates(String address) {
        Log.d(TAG, "Starting service");
        Intent intent = new Intent(getApplicationContext(), GeocodeIntentService.class);
        intent.putExtra(GeoConstant.RECEIVER, resultReceiver);
        intent.putExtra(GeoConstant.FETCH_TYPE_EXTRA, GeoConstant.ADDRESS);
        intent.putExtra(GeoConstant.LOCATION_NAME_DATA_EXTRA, address);
        Log.d(TAG, address);
        startService(intent);
    }

    public void onSubscribeAddClick(View v) {
        subscribeAddress = (addressTextField).getText().toString();
        getCoordinates(subscribeAddress);

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus())
            imm.hideSoftInputFromWindow(getCurrentFocus()
                    .getApplicationWindowToken(), 0);
        addressTextField.setText("");
        if (!subscribeAddress.isEmpty())
        Toast.makeText(this, "Subscribed to " + subscribeAddress, Toast.LENGTH_SHORT).show();
        /*
        if (zipcode.length() != 0 && zipcode.length() == 5 && zipcode.matches("[0-9]+")) {
            FirebaseMessaging.getInstance().subscribeToTopic(zipcode);
            Toast.makeText(getApplicationContext(), "Subcribed to " + zipcode, Toast.LENGTH_SHORT).show();
            addressTextField.setText("");
        } else
            Toast.makeText(getApplicationContext(), "Please enter correct zipcode", Toast.LENGTH_SHORT).show();
            */
    }

    public void readSharedPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String tmp;
        tmp = sharedPreferences.getString(Constant.PHONE_NUMBER, Dashboard.phoneNumber);
        Log.d(TAG, "saveContactInfoR: " + tmp);
        if (!tmp.isEmpty())
            userContact.setText(tmp);
        tmp = sharedPreferences.getString(Constant.EMERGENCY_NUMBER_1, tmp);
        if (!tmp.isEmpty())
            emergencyContact[0].setText(tmp);
        tmp = sharedPreferences.getString(Constant.EMERGENCY_NUMBER_2, tmp);
        if (!tmp.isEmpty())
            emergencyContact[1].setText(tmp);
        tmp = sharedPreferences.getString(Constant.EMERGENCY_NUMBER_3, tmp);
        if (!tmp.isEmpty())
            emergencyContact[2].setText(tmp);
        tmp = sharedPreferences.getString(Constant.EMERGENCY_NUMBER_4, tmp);
        if (!tmp.isEmpty())
            emergencyContact[3].setText(tmp);
    }

    public void resetNotification(View v) {
        FirebaseDatabase.getInstance().getReference("AllSubscriptions").removeValue();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.SUBSCRIBED_TO_GPS_LOCATION, null);
        Toast.makeText(this, "Reset all subscriptions", Toast.LENGTH_SHORT).show();
    }

    public void saveContactInfo(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
        editor = sharedPreferences.edit();
        editor.putString(Constant.PHONE_NUMBER, userContact.getText().toString());
        editor.putString(Constant.EMERGENCY_NUMBER_1, emergencyContact[0].getText().toString());
        editor.putString(Constant.EMERGENCY_NUMBER_2, emergencyContact[1].getText().toString());
        editor.putString(Constant.EMERGENCY_NUMBER_3, emergencyContact[2].getText().toString());
        editor.putString(Constant.EMERGENCY_NUMBER_4, emergencyContact[3].getText().toString());
        Log.d(TAG, "saveContactInfoW: " + userContact.getText().toString());
        editor.commit();
        //checkPhoneNumber();
        finish();
    }

    public void checkPhoneNumber(){
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(userContact.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }


    class AddressResultReceiver extends android.os.ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            Log.d(TAG, "onReceiveResult");
            if (resultCode == GeoConstant.SUCCESS_RESULT) { //when the thing is done, result is passed back here
                final Address address = resultData.getParcelable(GeoConstant.RESULT_DATA); //this retrieve the address
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {  //this part is where you put whatever you want to do
                        String longitude = address.getLongitude() + "";
                        String latitude = address.getLatitude() + "";
                        String topic = longitude + "_" + latitude;

                        Subscription newSub = new Subscription(subscribeAddress, distanceSpinner.getSelectedItem().toString(), topic);
                        DatabaseReference newChild = FirebaseDatabase.getInstance().getReference("AllSubscriptions").push();
                        newChild.setValue(newSub);
                        FirebaseMessaging.getInstance().subscribeToTopic(topic+"_"+distanceSpinner.getSelectedItem());
                        //FirebaseMessaging.getInstance().subscribeToTopic("notify");
                        subscribeAddress = "";
                    }
                });
            } else {
                Log.d(TAG, "Unable to find longitude latitude");
                Toast.makeText(getApplicationContext(), "Enter Valid Address", Toast.LENGTH_SHORT);
            }
        }
    }

}
