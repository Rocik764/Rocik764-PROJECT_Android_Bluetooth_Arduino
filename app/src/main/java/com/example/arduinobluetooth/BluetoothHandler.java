package com.example.arduinobluetooth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class BluetoothHandler extends Handler {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private String mConnectedDeviceName = null;

    public TextView textView;
    private Context context;

    // The Handler that gets information back from the BluetoothChatService
    public BluetoothHandler(TextView textView, Context context) {
        this.textView = textView;
        this.context = context;
    }

    public void sendToTarget() {
        throw new RuntimeException("Stub!");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            /*
                #41 -> spowrotem loop na MESSAGE_READ
            */
            case MESSAGE_WRITE:
                Log.i(TAG, "handleMessage: BLUETOOTHCHAT Message write");
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                textView.setText(writeMessage);
                break;
            /*
                #36 LOOP
            */
            case MESSAGE_READ:
                Log.i(TAG, "handleMessage: BLUETOOTHCHAT message read");
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                textView.setText(readMessage);
                break;
            /*
                #35
            */
            case MESSAGE_DEVICE_NAME:
                Log.i(TAG, "handleMessage: BLUETOOTHCHAT message device name");
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(context, "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Log.i(TAG, "handleMessage: BLUETOOTHCHAT message toast");
                Toast.makeText(context, msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
