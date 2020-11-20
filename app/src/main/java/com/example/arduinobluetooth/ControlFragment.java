package com.example.arduinobluetooth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ControlFragment extends Fragment {

    private TextView textView;
    private BluetoothChat mBluetoothchat;
    private BaseApp appState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: CONTROLFRAGMENT");
        super.onCreate(savedInstanceState);
        appState = ((BaseApp) Objects.requireNonNull(getActivity()).getApplication());
        mBluetoothchat = appState.getmChatService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChat(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothchat != null) {
            if (mBluetoothchat.getState() == BluetoothChat.STATE_NONE) {
                mBluetoothchat.start();
                mBluetoothchat.getmHandler().setTextView(textView);
            }
        } else mBluetoothchat = appState.getmChatService();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupChat(View view) {
        textView = view.findViewById(R.id.answer_txtview_ctrl);
        if(mBluetoothchat != null) mBluetoothchat.getmHandler().setTextView(textView);

        Button option0 = view.findViewById(R.id.option0_btn);
        option0.setOnClickListener(view0 -> buttonControl("0"));
        Button option1 = view.findViewById(R.id.option1_btn);
        option1.setOnClickListener(view1 -> buttonControl("1"));
        Button option2 = view.findViewById(R.id.option2_btn);
        option2.setOnClickListener(view2 -> buttonControl("2"));
        Button option3 = view.findViewById(R.id.option3_btn);
        option3.setOnClickListener(view3 -> buttonControl("3"));
        Button option4 = view.findViewById(R.id.option4_btn);
        option4.setOnClickListener(view4 -> buttonControl("4"));
        Button option5 = view.findViewById(R.id.option5_btn);
        option5.setOnClickListener(view5 -> buttonControl("5"));
        Button option6 = view.findViewById(R.id.option6_btn);
        option6.setOnClickListener(view6 -> buttonControl("6"));
        Button option7 = view.findViewById(R.id.option7_btn);
        option7.setOnClickListener(view7 -> buttonControl("7"));
        Button option8 = view.findViewById(R.id.option8_btn);
        option8.setOnClickListener(view8 -> buttonControl("8"));
        Button option9 = view.findViewById(R.id.option9_btn);
        option9.setOnClickListener(view9 -> buttonControl("9"));
        Button option10 = view.findViewById(R.id.option10_btn);
        option10.setOnClickListener(view10 -> buttonControl("10"));
        Button option11 = view.findViewById(R.id.option11_btn);
        option11.setOnClickListener(view11 -> buttonControl("11"));
        Button option12 = view.findViewById(R.id.option12_btn);
        option12.setOnClickListener(view12 -> buttonControl("12"));
        Button option13 = view.findViewById(R.id.option13_btn);
        option13.setOnClickListener(view13 -> buttonControl("13"));
        Button option14 = view.findViewById(R.id.option14_btn);
        option14.setOnClickListener(view14 -> buttonControl("14"));
    }

    private void buttonControl(String option) {
        if(mBluetoothchat != null) {
            if(mBluetoothchat.sendMessage(option)) {
                Toast.makeText(getActivity(), "Option " + option, Toast.LENGTH_SHORT).show();
            } else Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
        } else Toast.makeText(getActivity(),R.string.not_connected, Toast.LENGTH_LONG).show();
    }
}
