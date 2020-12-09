package com.example.arduinobluetooth;

import android.app.Application;
import android.util.Log;

/**
 * Global app state, contains my main BT connection object reference
 */
public class BaseApp extends Application {

    private static final String TAG = "MY_LOG ";
    public BluetoothChat mBluetoothchat;

    public BluetoothChat getmChat() {
        Log.i(TAG, "getmChat: BASEAPP");
        return this.mBluetoothchat;
    }

    public void setmChat(BluetoothChat bluetoothChat) {
        Log.i(TAG, "setmChat: BASEAPP");
       this.mBluetoothchat = bluetoothChat;
    }

    public void stopState() {
        Log.i(TAG, "stopState: BASEAPP");
        this.mBluetoothchat = null;
    }
}
