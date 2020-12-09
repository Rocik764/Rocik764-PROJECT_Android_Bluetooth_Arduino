package com.example.arduinobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChat {

    private static final String TAG = "MY_LOG ";
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothFragment";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final BluetoothHandler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Context context;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothFragment session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChat(BluetoothHandler handler, Context context) {
        Log.i(TAG, "BluetoothChat: constructor");
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = STATE_NONE;
        this.mHandler = handler;
        this.context = context;
    }

    public BluetoothHandler getmHandler() {
        return this.mHandler;
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
        // Check that we're actually connected before trying anything
        if (getState() != BluetoothChat.STATE_CONNECTED) {
            return;
        }
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
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
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
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
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
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
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
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        error("Unable to connect device");
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.i(TAG, "connectionLost: BLUETOOTHCHAT");
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        error("Device connection was lost");
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            Log.i(TAG, "AcceptThread: BLUETOOTHCHAT");
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                error("Something went wrong");
                System.out.println(e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "run: AcceptThread BLUETOOTHCHAT");
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    error("Something went wrong");
                    System.out.println(e.toString());
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChat.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    error("Something went wrong");
                                    System.out.println(e.toString());
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            Log.i(TAG, "cancel: AcceptThread BLUETOOTHCHAT");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                error("Something went wrong");
                System.out.println(e.toString());
            }
        }
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
                error("Something went wrong");
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
                error("Something went wrong");
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    error("Something went wrong");
                    System.out.println(e2.toString());
                }
                // Start the service over to restart listening mode
                BluetoothChat.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothChat.this) {
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
                error("Something went wrong");
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
                error("Something went wrong");
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
                error("Something went wrong");
                System.out.println(e.toString());
            }
        }

        public void cancel() {
            Log.i(TAG, "cancel: ConnectedThread BLUETOOTHCHAT");
            try {
                mmSocket.close();
            } catch (IOException e) {
                error("Something went wrong");
                System.out.println(e.toString());
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
}
