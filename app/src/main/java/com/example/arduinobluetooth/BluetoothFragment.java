package com.example.arduinobluetooth;

    /*
        OPISAC ARDUINO, JAK PRZECHODZI INFO PRZEZ BT DO NIEGO Z ANDROIDA
        pełna dokumentacja + katalog z projektem aplikacji
        biblioteki schematy
        do 15 stycznia
     */
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class BluetoothFragment extends Fragment {

    public BluetoothChat mBluetoothchat;
    public Button mSendButton;
    public TextView textView;
    public EditText mOutEditText;
    private StringBuffer mOutStringBuffer;
    private BaseApp appState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: BLUETOOTHCHAT");
        super.onCreate(savedInstanceState);
        appState = ((BaseApp) Objects.requireNonNull(getActivity()).getApplication());
        mBluetoothchat = appState.getmChatService();
    }
    /*
            #1
         */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: BLUETOOTHCHAT");
        super.onCreate(savedInstanceState);
        //container is a parent view where my fragment fragment_bt gets its layout parent from
        return inflater.inflate(R.layout.fragment_bt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChat(view);
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart: BLUETOOTHCHAT");
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
        Log.i(TAG, "onResume: BLUETOOTHCHAT");
        super.onResume();
        if (mBluetoothchat != null) {
            if (mBluetoothchat.getState() == BluetoothChat.STATE_NONE) {
                mBluetoothchat.start();
                mBluetoothchat.getmHandler().setTextView(textView);
            }
        } else mBluetoothchat = appState.getmChatService();
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
        //if (mBluetoothchat != null) mBluetoothchat.stop();
    }

    @SuppressLint("CutPasteId")
    public void setupChat(View view) {
        Log.i(TAG, "setupChat: MAIN");
        textView = view.findViewById(R.id.answer_txtview_bt);
        if(mBluetoothchat != null) mBluetoothchat.getmHandler().setTextView(textView);

        mOutEditText = view.findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = view.findViewById(R.id.button_send);
        mSendButton.setOnClickListener(v -> {
            if(mBluetoothchat != null) {
                TextView editTextView = view.findViewById(R.id.edit_text_out);
                String message = editTextView.getText().toString();
                if(mBluetoothchat.sendMessage(message)) {
                    // Reset out string buffer to zero and clear the edit text field
                    mOutStringBuffer.setLength(0);
                    mOutEditText.setText(mOutStringBuffer);
                } else Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();

            } else Toast.makeText(getActivity(),R.string.not_connected, Toast.LENGTH_LONG).show();
        });
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    public TextView.OnEditorActionListener mWriteListener =

            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    Log.i(TAG, "onEditorAction: MAIN");
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        mBluetoothchat.sendMessage(message);
                    }
                    return true;
                }
            };
}
