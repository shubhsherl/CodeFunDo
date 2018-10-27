package com.shubh.watcherth.BaseClass;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.shubh.watcherth.BluetoothChatService;
import com.shubh.watcherth.R;
import com.shubh.watcherth.ReportObject.CompactReport;
import com.shubh.watcherth.ReportObject.Packet;
import com.shubh.watcherth.XBeeManager;
import com.shubh.watcherth.XBeeManagerApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseXbeeActivity extends AppCompatActivity implements IDataReceiveListener{

    private XBeeManager xbeeManager;
    public final String TAG = BaseXbeeActivity.class.getSimpleName();

    private int receivingMethod = 0;

    // Message types sent from the BluetoothMessageService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothMessageService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_xbee);
        xbeeManager = XBeeManagerApplication.getInstance().getXBeeManager();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter.isEnabled()) {
            mChatService = new BluetoothChatService(BaseXbeeActivity.this, mHandler);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (xbeeManager.getLocalXBeeDevice() != null && xbeeManager.getLocalXBeeDevice().isOpen()) {
            xbeeManager.subscribeDataPacketListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void dataReceived(XBeeMessage xbeeMessage){
        showToastMessage("Received data from XBee channel");
        receivingMethod = 2;
        String data = new String(xbeeMessage.getData());
        receiveDataFromChannel(data);
    }

    // Receive data over XBE/BLE and upload to Firebase
    public void receiveDataFromChannel(String data){

        List<String> pathToServer = null;
        Packet newPacket = null;

        Gson gson = new Gson();
        CompactReport cmpReport = gson.fromJson(data, CompactReport.class);

        boolean isConnected = checkInternetConnection();

        if(receivingMethod == 2) {
            // On Receiving the data, convert it to object and add the XBEE device id in path to server
            pathToServer = cmpReport.getPathToServer();
            String receiverDeviceAddress = xbeeManager.getLocalXBee64BitAddress().toString();
            pathToServer.add(receiverDeviceAddress);
        }

        if (isConnected) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
            String timestamp = simpleDateFormat.format(new Date());
            cmpReport.setTimestamp(timestamp);

            if(receivingMethod == 2) {
                newPacket = new Packet(pathToServer, FirebaseInstanceId.getInstance().getToken());
                // Saving the path to Firebase without report ID. Need to add report id afterwards
                DatabaseReference path = FirebaseDatabase.getInstance().getReference("path").push();
                path.setValue(newPacket);
            }

            DatabaseReference newChild = FirebaseDatabase.getInstance().getReference("Reports").push();
            newChild.setValue(cmpReport);

            Toast.makeText(this, "Report uploaded to Internet", Toast.LENGTH_LONG).show();

        } else {

            if(receivingMethod == 3){

                if (xbeeManager.getLocalXBeeDevice() != null && xbeeManager.getLocalXBeeDevice().isOpen()) {

                    pathToServer = cmpReport.getPathToServer();

                    if(pathToServer == null){
                        pathToServer = new ArrayList<String>();
                    }

                    String receiverDeviceAddress = xbeeManager.getLocalXBee64BitAddress().toString();
                    pathToServer.add(receiverDeviceAddress);
                    cmpReport.setPathToServer(pathToServer);
                }
            }
            data = gson.toJson(cmpReport);
            xbeeBroadcast(data);
        }

    }

    public void xbeeBroadcast(String data){

        final String reportData = data;
        Log.d(TAG, "broadcast");

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(xbeeManager.getLocalXBeeDevice().isOpen()) {
                        String DATA_TO_SEND = reportData;
                        byte[] dataToSend = DATA_TO_SEND.getBytes();

                        xbeeManager.broadcastData(dataToSend);

                        Log.d(TAG, "Broadcasting ");
                        showToastMessage("Device opened and data sent: " + xbeeManager.getLocalXBeeDevice().toString());
                    }else Log.d(TAG, "XBee is not opened.");
                } catch (XBeeException e) {
                    Log.d("Xbee exception ", e.toString());
                }
            }
        });
        sendThread.start();
    }

    // To check whether device has an active Internet connection
    public boolean checkInternetConnection(){

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(this!=null)
                    Toast.makeText(BaseXbeeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            FragmentActivity activity = BaseXbeeActivity.this;

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Log.i("MessageSTATE", "-->CONNECTED");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Log.i("MessageSTATE", "-->CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            Log.i("MessageSTATE", "-->LISTENING");
                        case BluetoothChatService.STATE_NONE:
                            Log.i("MessageSTATE", "-->NONE");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(activity,readMessage, Toast.LENGTH_LONG).show();
                    receivingMethod = 3;
                    receiveDataFromChannel(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}
