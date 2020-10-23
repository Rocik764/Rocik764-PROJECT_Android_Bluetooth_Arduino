package com.example.arduinobluetooth;

    /*
        OPISAC ARDUINO, JAK PRZECHODZI INFO PRZEZ BT DO NIEGO Z ANDROIDA
        pełna dokumentacja + katalog z projektem aplikacji
        biblioteki schematy
        do 15 stycznia
     */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class BluetoothChat extends Activity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private EditText mOutEditText;
    private Button mSendButton;
    private TextView textView;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    //
    private BluetoothHandler mBluetoothHandler = null;

    public int counter = 0;

    private List messageList = new ArrayList();

    /*
        #1
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: BLUETOOTHCHAT");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //  getting default BT adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    /*
        #2
        #16
        #27
     */
    @Override
    public void onStart() {
        Log.i(TAG, "onStart: BLUETOOTHCHAT");
        super.onStart();
        if (mChatService == null) setupChat();
    }

    /*
        #3
        #8
        #17
        #28
     */
    @Override
    public synchronized void onResume() {
        Log.i(TAG, "onResume: BLUETOOTHCHAT");
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    /*
        #6
     */
    /*
        Sets up whole chat. Send button gets string from TextView and calls sendMessage
     */
    private void setupChat() {
        Log.i(TAG, "setupChat: BLUETOOTHCHAT");
        textView = (TextView) findViewById(R.id.textViewTest);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize BluetoothHandler
        mBluetoothHandler = new BluetoothHandler(textView, getApplicationContext());

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mBluetoothHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /*
        #4
        #14
        #20
     */
    @Override
    public synchronized void onPause() {
        Log.i(TAG, "onPause: BLUETOOTHCHAT");
        super.onPause();

    }

    /*
        #15
        #21
    */
    @Override
    public void onStop() {
        Log.i(TAG, "onStop: BLUETOOTHCHAT");
        super.onStop();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: BLUETOOTHCHAT");
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }

    /*
        #37
    */
    private void sendMessage(String message) {
        Log.i(TAG, "sendMessage: BLUETOOTHCHAT");
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    Log.i(TAG, "onEditorAction: BLUETOOTHCHAT");
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    return true;
                }
            };

    /*
        #5
        #22
     */
    /*
        REQUEST_ENABLE_BT goes first -> when app requests BT to be enabled and user enables it or not (pressing Yes/No)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: BLUETOOTHCHAT");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /*
        #19
    */
    public void connect(View v) {
        Log.i(TAG, "connect: BLUETOOTHCHAT");
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /*
        Turning on discoverable mode in android
     */
    public void discoverable(View v) {
        Log.i(TAG, "discoverable: BLUETOOTHCHAT");
        ensureDiscoverable();
    }

    private void ensureDiscoverable() {
        Log.i(TAG, "ensureDiscoverable: BLUETOOTHCHAT");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /*
        Turning On or Off BT connection with a Button onClick -> starts intent with code = 2 / onActivityResult: 270-279
     */
    public void bluetoothOnOff(View v) {
        Log.i(TAG, "bluetoothOnOff: BLUETOOTHCHAT");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.disable();
        }
    }
}
