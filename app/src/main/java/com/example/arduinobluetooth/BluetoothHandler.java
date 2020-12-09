package com.example.arduinobluetooth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothHandler extends Handler {

    private static final String TAG = "MY_LOG ";
    // Message types sent from the BluetoothChat Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChat Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public TextView textView;
    private Context context;

    // The Handler that gets information back from the BluetoothChat
    public BluetoothHandler(Context context) {
        Log.i(TAG, "BluetoothHandler: constructor");
        this.context = context;
    }

    /**
     * automatically handles messages in switch case. Gets the sent message data
     * and sets textview "Answer" to what's being send to arduino
     */
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WRITE:
                Log.i(TAG, "BluetoothHandler: handleMessage: BLUETOOTHCHAT Message write");
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                if(textView == null) break;
                else textView.setText(writeMessage);
                break;
            case MESSAGE_READ:
                if(textView != null) {
                    byte[] readBuf = (byte[]) msg.obj;
                    //construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    textView.setText(readMessage);
                } else break;
                break;
            case MESSAGE_DEVICE_NAME:
                Log.i(TAG, "BluetoothHandler: handleMessage: BLUETOOTHCHAT message device name");
                // save the connected device's name
                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(context, "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Log.i(TAG, "BluetoothHandler: handleMessage: BLUETOOTHCHAT message toast");
                Toast.makeText(context, msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void setTextView(TextView textView) {
        Log.i(TAG, "setTextView: BluetoothHandler");
        this.textView = textView;
    }
}
