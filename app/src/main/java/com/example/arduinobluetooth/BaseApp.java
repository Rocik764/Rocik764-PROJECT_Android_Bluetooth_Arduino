package com.example.arduinobluetooth;

import android.app.Application;

public class BaseApp extends Application {

    public BluetoothChat mBluetoothchat;

    public BluetoothChat getmChatService() {
        return this.mBluetoothchat;
    }

    public void setmChatService(BluetoothChat bluetoothChat) {
       this.mBluetoothchat = bluetoothChat;
    }

    public void stopState() {
        this.mBluetoothchat = null;
    }
}
