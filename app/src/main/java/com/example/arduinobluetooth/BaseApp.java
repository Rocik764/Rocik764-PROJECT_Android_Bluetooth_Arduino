package com.example.arduinobluetooth;

import android.app.Application;

public class BaseApp extends Application {

    public BluetoothChatService mChatService;

    public BluetoothChatService getmChatService() {
        return mChatService;
    }

    public void setmChatService(BluetoothChatService bluetoothChatService) {
        mChatService = bluetoothChatService;
    }

}
