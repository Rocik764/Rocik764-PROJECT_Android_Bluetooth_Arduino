package com.example.arduinobluetooth;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import java.util.UUID;

/**
 * Global app state, contains my main BT connection object reference
 */
public class BaseApp extends Application {

    public static final String TAG = "MY_LOG: ";
    public static final String CHANNEL_1_ID = "Bluetooth Service Channel";
    public static final String SERVICE_NOTIFICATION_TITLE = "Bluetooth Service";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_START = "Waiting for connection";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_CONNECTING = "Connecting to ";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_CONNECTED = "Connected to ";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_STOPPED = "Bluetooth Service has been stopped";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_DISCONNECTED = "Disconnected";
    public static final String SERVICE_NOTIFICATION_DESCRIPTION_CONNECTION_FAILED = "Connection failed";

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device

    // Unique UUID for this application
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}
