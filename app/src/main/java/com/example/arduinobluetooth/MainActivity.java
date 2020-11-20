package com.example.arduinobluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static DrawerLayout drawer;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public BaseApp appState;

    // Local Bluetooth adapter
    public BluetoothAdapter mBluetoothAdapter;

    // Member object for the chat services
    public BluetoothChat mBluetoothchat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: MAIN");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        appState = ((BaseApp)this.getApplication());

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BluetoothFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_bluetooth);
        }
    }

    /*
        #2
        #16
        #27
     */
    @Override
    public void onStart() {
        Log.i(TAG, "onStart: MAIN");
        super.onStart();
    }

    /*
        #3
        #8
        #17
        #28
     */
    @Override
    public synchronized void onResume() {
        Log.i(TAG, "onResume: MAIN");
        super.onResume();
        if (mBluetoothchat != null) {
            if (mBluetoothchat.getState() == BluetoothChat.STATE_NONE) {
                mBluetoothchat.start();
            }
        }
    }
    /*
        #6
     */
    /*
        #4
        #14
        #20
     */
    @Override
    public synchronized void onPause() {
        Log.i(TAG, "onPause: MAIN");
        super.onPause();

    }

    /*
        #15
        #21
    */
    @Override
    public void onStop() {
        Log.i(TAG, "onStop: MAIN");
        super.onStop();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: MAIN");
        super.onDestroy();
        // Stop the Bluetooth chat services
        //if (mBluetoothchat != null) mBluetoothchat.stop();
    }

    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: MAIN");
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
        #5
        #22
     */
    /*
        REQUEST_ENABLE_BT goes first -> when app requests BT to be enabled and user enables it or not (pressing Yes/No)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: MAIN");
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: BLUETOOTHCHAT");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = Objects.requireNonNull(data.getExtras()).getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    setupChat();
                    mBluetoothchat.connect(device);
                    appState.setmChatService(mBluetoothchat);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /*
        Sets up whole chat. Send button gets string from TextView and calls sendMessage
    */
    private void setupChat() {
        Log.i(TAG, "setupService: MAIN");

        // Initialize BluetoothHandler
        BluetoothHandler mBluetoothHandler = new BluetoothHandler(getApplicationContext());

        // Initialize the BluetoothChat to perform bluetooth connections
        mBluetoothchat = new BluetoothChat(mBluetoothHandler,this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.i(TAG, "onNavigationItemSelected: MAIN");
        switch (menuItem.getItemId()) {
            case R.id.nav_bluetooth:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BluetoothFragment()).commit();
                break;
            case R.id.nav_control:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ControlFragment()).commit();
            case R.id.nav_exit:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: MAIN");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected: MAIN");
        switch (item.getItemId()) {
            case R.id.search:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.onOff:
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    mBluetoothAdapter.disable();
                }
                return true;
            case R.id.disconnect:
                mBluetoothchat.stop();
                appState.stopState();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}