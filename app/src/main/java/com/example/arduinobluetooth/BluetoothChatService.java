package com.example.arduinobluetooth;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static com.example.arduinobluetooth.BaseApp.CHANNEL_1_ID;
import static com.example.arduinobluetooth.BaseApp.MY_UUID;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_CONNECTED;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_CONNECTING;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_CONNECTION_FAILED;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_DISCONNECTED;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_START;
import static com.example.arduinobluetooth.BaseApp.SERVICE_NOTIFICATION_DESCRIPTION_STOPPED;
import static com.example.arduinobluetooth.BaseApp.STATE_CONNECTED;
import static com.example.arduinobluetooth.BaseApp.STATE_CONNECTING;
import static com.example.arduinobluetooth.BaseApp.STATE_NONE;
import static com.example.arduinobluetooth.BaseApp.TAG;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService extends Service {

    private final IBinder mBinder = new MyBinder();
    // Member fields
    private BluetoothAdapter mAdapter;
    private BluetoothHandler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private NotificationManagerCompat notificationManager;

    /**
     * Constructor. Prepares a new BluetoothFragment session.
     */
    public BluetoothChatService() {
        Log.i(TAG, "BluetoothChat: constructor");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "BluetoothChat: onCreate");
        super.onCreate();
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
        this.mHandler = new BluetoothHandler();
        notificationManager = NotificationManagerCompat.from(this);
        start();
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_START);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public class MyBinder extends Binder {

        BluetoothChatService getService(){
            return BluetoothChatService.this;
        }
    }

    public BluetoothHandler getmHandler() {
        return this.mHandler;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: called.");
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_STOPPED);
        super.onDestroy();
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.i(TAG, "setState: BLUETOOTHCHAT: " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothHandler.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Returns the current connection state.
     */
    public synchronized int getState() {
        Log.i(TAG, "getState: BLUETOOTHCHAT: " + mState);
        return mState;
    }

    /**
     * If we're connected, this method gets bytes of string in parameter and uses write() method
     * down below
     */
    public void sendMessage(String message) {
        Log.i(TAG, "sendMessage: BLUETOOTHCHAT");
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChat to write
            byte[] send = message.getBytes();
            write(send);
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.i(TAG, "start: BLUETOOTHCHAT");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.i(TAG, "connect: BLUETOOTHCHAT");
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_CONNECTING + device.getName());
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.i(TAG, "connected: BLUETOOTHCHAT");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothHandler.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothHandler.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_CONNECTED + device.getName());
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.i(TAG, "stop: BLUETOOTHCHAT");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        Log.i(TAG, "write: BLUETOOTHCHAT");
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.i(TAG, "connectionFailed: BLUETOOTHCHAT");
        setState(STATE_NONE);
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_CONNECTION_FAILED);
        // Send a failure message back to the Activity
        error("Unable to connect to the device");
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.i(TAG, "connectionLost: BLUETOOTHCHAT");
        setState(STATE_NONE);
        sendNotification(SERVICE_NOTIFICATION_DESCRIPTION_DISCONNECTED);
        // Send a failure message back to the Activity
        error("Device connection was lost");
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            Log.i(TAG, "ConnectThread: BLUETOOTHCHAT");
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                error(e.getMessage());
                System.out.println(e.toString());
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "run: ConnectThread BLUETOOTHCHAT");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.d("error: ", e.toString());
                error(e.getMessage());
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    error(e.getMessage());
                    System.out.println(e2.toString());
                }
                // Start the service over to restart listening mode
                BluetoothChatService.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            Log.i(TAG, "cancel: ConnectThread BLUETOOTHCHAT");
            try {
                mmSocket.close();
            } catch (IOException e) {
                error(e.getMessage());
                System.out.println(e.toString());
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.i(TAG, "ConnectedThread: ConnectedThread BLUETOOTHCHAT");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                error(e.getMessage());
                System.out.println(e.toString());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "run: ConnectedThread BLUETOOTHCHAT");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(BluetoothHandler.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            Log.i(TAG, "write: ConnectedThread BLUETOOTHCHAT");
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothHandler.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                error(e.getMessage());
            }
        }

        public void cancel() {
            Log.i(TAG, "cancel: ConnectedThread BLUETOOTHCHAT");
            try {
                mmSocket.close();
            } catch (IOException e) {
                error(e.getMessage());
            }
        }
    }

    /**
     * Method creates a bundle object and adds data to it. Next, it'll send it
     * through the handler to the Main Thread UI
     */
    private void error(String message) {
        Log.i(TAG, "error: BLUETOOTHCHAT");
        Message msg = mHandler.obtainMessage(BluetoothHandler.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothHandler.TOAST, message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void sendNotification(String description) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_bluetooth)
                .setContentTitle(BaseApp.SERVICE_NOTIFICATION_TITLE)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1, notification);
    }
}
