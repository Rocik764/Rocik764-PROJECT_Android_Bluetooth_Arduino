package com.example.arduinobluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import static com.example.arduinobluetooth.BaseApp.STATE_CONNECTED;
import static com.example.arduinobluetooth.BaseApp.STATE_CONNECTING;
import static com.example.arduinobluetooth.BaseApp.TAG;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static DrawerLayout drawer;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isBound = false;

    //private Intent intent;

    // Local Bluetooth adapter
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothChatService bluetoothChatService;
    // Member object for the bluetooth connection
    //public BluetoothChat mBluetoothchat;

    /**
     * Initializes and sets up toolbar's and navigation drawer's UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

    @Override
    public void onStart() {
        Log.i(TAG, "onStart: MAINACTIVITY");
        Intent intent = new Intent(this, BluetoothChatService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothChatService.MyBinder binder = (BluetoothChatService.MyBinder) iBinder;
            bluetoothChatService = binder.getService();
            bluetoothChatService.getmHandler().setContext(MainActivity.super.getApplicationContext());
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    /**
     * onResume - makes sure to keep up BluetoothChat's object's reference
     */
    @Override
    public synchronized void onResume() {
        Log.i(TAG, "onResume: MAINACTIVITY");
        super.onResume();
    }

    @Override
    public synchronized void onPause() {
        Log.i(TAG, "onPause: MAINACTIVITY");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: MAINACTIVITY");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: MAINACTIVITY");
        super.onDestroy();
        if(isBound) unbindService(serviceConnection);
    }

    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: MAINACTIVITY");
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This method awaits for callbacks from activities. After choosing the device to start
     * connection, this method will activate and get the intent from DeviceListActivity.
     * If the state isn't already in connecting stage it'll get MAC address out of the intent
     * create BluetoothDevice object, get info about the PAIRED device from hardware's
     * bluetooth adapter using it's address and start connecting to it using BluetoothChat's
     * instance that has been initialized in setupChat() method. At the end it'll set the instance
     * to our main connection object inside BaseApp class so that we can always call it from
     * different fragments or activity around the application.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: MAINACTIVITY");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONNECT_DEVICE) {
            if (bluetoothChatService != null && bluetoothChatService.getState() == STATE_CONNECTING) {
                Toast.makeText(this, "Already connecting", Toast.LENGTH_SHORT).show();
            } else if (bluetoothChatService != null && bluetoothChatService.getState() == STATE_CONNECTED) {
                Toast.makeText(this, "Already connected", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = Objects.requireNonNull(data.getExtras()).getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                Log.i(TAG, "onActivityResult: MAINACTIVITY: address: " + address + " name: " + device.getName());
                bluetoothChatService.connect(device);
            }
        }
    }

    /**
     * Listener for navigation drawer. When user switches between fragments, this method is called
     * and opens the fragment that has been clicked.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.i(TAG, "onNavigationItemSelected: MAINACTIVITY");
        switch (menuItem.getItemId()) {
            case R.id.nav_bluetooth:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BluetoothFragment()).commit();
                break;
            case R.id.nav_control:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalculatorFragment()).commit();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Creates menu's options on the toolbar
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: MAINACTIVITY");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);
        return true;
    }

    /**
     * Listener for main toolbar. It checks which item has been clicked from the toolbar's menu
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected: MAINACTIVITY");
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
                bluetoothChatService.stop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}