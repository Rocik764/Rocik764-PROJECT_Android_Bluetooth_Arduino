package com.example.arduinobluetooth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import static com.example.arduinobluetooth.BaseApp.TAG;

public class BluetoothFragment extends Fragment {

    public BluetoothChatService bluetoothChatService;
    public Button mSendButton;
    public TextView textView;
    public EditText mOutEditText;
    private StringBuffer mOutStringBuffer;

    /**
     * onCreate - initializes BaseApp's object, gets it's instance and then initializes
     * main connection object using getmChat() method from BaseApp
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: BLUETOOTH_FRAGMENT");
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity != null) bluetoothChatService = mainActivity.bluetoothChatService;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: BLUETOOTH_FRAGMENT");
        super.onCreate(savedInstanceState);
        //container is a parent view where my fragment fragment_bt gets its layout parent from
        return inflater.inflate(R.layout.fragment_bt, container, false);
    }

    /**
     * onViewCreated - initializes every UI's objects using setupChat() method
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: BLUETOOTH_FRAGMENT");
        super.onViewCreated(view, savedInstanceState);
        setupChat(view);
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart: BLUETOOTH_FRAGMENT");
        if(bluetoothChatService != null) {
            Log.i(TAG, "onStart: BLUETOOTH_FRAGMENT NOT NULL textView");
            bluetoothChatService.getmHandler().setTextView(textView);
        }
        super.onStart();
    }

    /**
     * onResume - makes sure to keep up BluetoothChat's object's reference
     */
    @Override
    public synchronized void onResume() {
        Log.i(TAG, "onResume: BLUETOOTH_FRAGMENT");
        super.onResume();
        if (bluetoothChatService == null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if(mainActivity != null) bluetoothChatService = mainActivity.bluetoothChatService;
        }
    }

    @Override
    public synchronized void onPause() {
        Log.i(TAG, "onPause: BLUETOOTH_FRAGMENT");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: BLUETOOTH_FRAGMENT");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: BLUETOOTH_FRAGMENT");
        super.onDestroy();
    }

    /**
     * initializes every UI's objects. Edit text field, on button click listener:
     * when user clicks the button, it gets text from the text field, adds new line character
     * and cleans edit text field
     */
    @SuppressLint("CutPasteId")
    public void setupChat(View view) {
        Log.i(TAG, "setupChat: BLUETOOTH_FRAGMENT");
        textView = view.findViewById(R.id.answer_txtview_bt);
        mOutEditText = view.findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = view.findViewById(R.id.button_send);
        mSendButton.setOnClickListener(v -> {
            if(bluetoothChatService != null) {
                Log.i(TAG, "setupChat: BLUETOOTH_FRAGMENT setOnClickListener mSendButton");
                TextView editTextView = view.findViewById(R.id.edit_text_out);
                String message = editTextView.getText().toString();
                message += "\n";
                bluetoothChatService.sendMessage(message);
                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);
            } else Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_LONG).show();
        });
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }
//
//    public TextView.OnEditorActionListener mWriteListener =
//            new TextView.OnEditorActionListener() {
//                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//                    Log.i(TAG, "onEditorAction: BLUETOOTH_FRAGMENT");
//                    // If the action is a key-up event on the return key, send the message
//                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
//                        String message = view.getText().toString();
//                        mBluetoothchat.sendMessage(message);
//                    }
//                    return true;
//                }
//            };
}
