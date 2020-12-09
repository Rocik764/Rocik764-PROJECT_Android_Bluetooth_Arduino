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

public class CalculatorFragment extends Fragment {

    private static final String TAG = "MY_LOG ";
    private TextView textView;
    private TextView textViewInput;
    private TextView textViewError;
    private BluetoothChat mBluetoothchat;
    private Calculator calculator;
    private BaseApp appState;

    /**
     * onCreate - initializes BaseApp's object, gets it's instance and then initializes
     * main connection object using getmChat() method from BaseApp.
     * Initializes Calculator's class constructor in order to perform mathematical operations
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: CALCULATOR_FRAGMENT");
        super.onCreate(savedInstanceState);
        appState = ((BaseApp) Objects.requireNonNull(getActivity()).getApplication());
        mBluetoothchat = appState.getmChat();
        calculator = new Calculator();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: CALCULATOR_FRAGMENT");
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    /**
     * onViewCreated - initializes every UI's objects using setupChat() method
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: CALCULATOR_FRAGMENT");
        super.onViewCreated(view, savedInstanceState);
        setupChat(view);
    }

    /**
     * onResume - makes sure to keep up BluetoothChat's object's reference
     */
    @Override
    public void onResume() {
        Log.i(TAG, "onResume: CALCULATOR_FRAGMENT");
        super.onResume();
        if (mBluetoothchat != null) {
            if (mBluetoothchat.getState() == BluetoothChat.STATE_NONE) {
                mBluetoothchat.start();
                mBluetoothchat.getmHandler().setTextView(textView);
            }
        } else mBluetoothchat = appState.getmChat();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: CALCULATOR_FRAGMENT");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop: CALCULATOR_FRAGMENT");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: CALCULATOR_FRAGMENT");
        super.onDestroy();
    }

    /**
     * initializes every UI's objects. Every button has it's own
     * listener with static parameter that's being send
     * to method ButtonClick()
     */
    private void setupChat(View view) {
        Log.i(TAG, "setupChat: CALCULATOR_FRAGMENT");
        textView = view.findViewById(R.id.answer_txtview_ctrl);
        textViewInput = view.findViewById(R.id.input_txtview_ctrl);
        textViewError = view.findViewById(R.id.error_txtview_ctrl);
        if(mBluetoothchat != null) mBluetoothchat.getmHandler().setTextView(textView);

        Button option0 = view.findViewById(R.id.option16_btn);
        option0.setOnClickListener(view1 -> ButtonClick("0"));
        Button option1 = view.findViewById(R.id.option1_btn);
        option1.setOnClickListener(view1 -> ButtonClick("1"));
        Button option2 = view.findViewById(R.id.option2_btn);
        option2.setOnClickListener(view2 -> ButtonClick("2"));
        Button option3 = view.findViewById(R.id.option3_btn);
        option3.setOnClickListener(view3 -> ButtonClick("3"));
        Button option4 = view.findViewById(R.id.option4_btn);
        option4.setOnClickListener(view4 -> ButtonClick("4"));
        Button option5 = view.findViewById(R.id.option5_btn);
        option5.setOnClickListener(view5 -> ButtonClick("5"));
        Button option6 = view.findViewById(R.id.option6_btn);
        option6.setOnClickListener(view6 -> ButtonClick("6"));
        Button option7 = view.findViewById(R.id.option7_btn);
        option7.setOnClickListener(view7 -> ButtonClick("7"));
        Button option8 = view.findViewById(R.id.option8_btn);
        option8.setOnClickListener(view8 -> ButtonClick("8"));
        Button option9 = view.findViewById(R.id.option9_btn);
        option9.setOnClickListener(view9 -> ButtonClick("9"));
        Button option10 = view.findViewById(R.id.option10_btn);
        option10.setOnClickListener(view10 -> ButtonClick("+"));
        Button option11 = view.findViewById(R.id.option11_btn);
        option11.setOnClickListener(view11 -> ButtonClick("-"));
        Button option12 = view.findViewById(R.id.option12_btn);
        option12.setOnClickListener(view12 -> ButtonClick("x"));
        Button option13 = view.findViewById(R.id.option13_btn);
        option13.setOnClickListener(view13 -> ButtonClick("/"));
        Button option14 = view.findViewById(R.id.option14_btn);
        option14.setOnClickListener(view14 -> ButtonClick("="));
        Button option15 = view.findViewById(R.id.option15_btn);
        option15.setOnClickListener(view14 -> ButtonClick("."));
        Button option17 = view.findViewById(R.id.option17_btn);
        option17.setOnClickListener(view14 -> ButtonClick("AC"));
        Button option18 = view.findViewById(R.id.option18_btn);
        option18.setOnClickListener(view14 -> ButtonClick("^"));
        Button option19 = view.findViewById(R.id.option19_btn);
        option19.setOnClickListener(view14 -> ButtonClick("⬅"));
    }

    /**
     * Depending on which parameter has been passed to the method, it will perform
     * different mathematical operations
     */
    public void ButtonClick(String option){
        Log.i(TAG, "ButtonClick: CALCULATOR_FRAGMENT");
        switch (option){
            case "AC":
                calculator.setInput("");
                calculator.setError("");
                textViewInput.setText(calculator.getinput());
                updateArduino();
                break;
            case "x":
                calculator.setClearResult(false);
                if(calculator.Solve()) textViewInput.setText(calculator.getinput());
                else textViewError.setText(calculator.getError());
                calculator.setInput(calculator.getinput() + "*");
                textViewInput.setText(calculator.getinput());
                updateArduino();
                break;
            case "^":
                calculator.setClearResult(false);
                if(calculator.Solve()) textViewInput.setText(calculator.getinput());
                else textViewError.setText(calculator.getError());
                calculator.setInput(calculator.getinput() + "^");
                textViewInput.setText(calculator.getinput());
                updateArduino();
                break;
            case "=":
                calculator.setClearResult(true);
                if(calculator.Solve()) textViewInput.setText(calculator.getinput());
                else textViewError.setText(calculator.getError());
                calculator.setAnswer(calculator.getinput());
                updateArduino();
                break;
            case "⬅":
                if(calculator.getinput().length() > 0){
                    calculator.setClearResult(false);
                    String newText = calculator.getinput().substring(0, calculator.getinput().length()-1);
                    calculator.setInput(newText);
                    textViewInput.setText(calculator.getinput());
                    updateArduino();
                }
                break;
            default:
                if(calculator.getinput() == null){
                    calculator.setInput("");
                }
                if(option.equals("+") || option.equals("-") || option.equals("/")) {
                    calculator.setClearResult(false);
                    if(calculator.Solve()) textViewInput.setText(calculator.getinput());
                    else textViewError.setText(calculator.getError());
                    updateArduino();
                }
                else if(calculator.getClearResult() == true){
                    calculator.setInput("");
                    calculator.setClearResult(false);
                }
                calculator.setInput(calculator.getinput() + option);
                textViewInput.setText(calculator.getinput());
                updateArduino();
        }

    }

    /**
     * if connected, it'll check if error field is empty, if it isn't, then it'll send
     * error field's content to arduino. If it's empty, it'll get content out of input
     * field and pass it to the arduino using BluetoothChat's instance
     */
    private void updateArduino() {
        Log.i(TAG, "updateArduino: CALCULATOR_FRAGMENT");
        if(mBluetoothchat != null) {
            String message;
            if(calculator.getError().equals("")) message = calculator.getinput();
            else message = calculator.getError();
            message += "\n";
            mBluetoothchat.sendMessage(message);
        } else Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
    }
}
