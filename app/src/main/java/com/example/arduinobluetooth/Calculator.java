package com.example.arduinobluetooth;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class Calculator {

    private String input;
    private String error;
    private String answer;
    private boolean clearResult;
    private String[] chars = {"+", "-", "*", "/", "."};

    public Calculator() {
        Log.i(TAG, "Calculator: ");
        this.input = "";
        this.error = "";
    }

    public String getinput() {
        Log.i(TAG, "getinput: ");
        return this.input;
    }
    public String getError() {
        Log.i(TAG, "getError: ");
        return this.error;
    }
    public void setError(String error) {
        Log.i(TAG, "setError: ");
        this.error = error;
    }
    public void setInput(String input) {
        Log.i(TAG, "setInput: ");
        this.input = input;
    }
    public boolean getClearResult() {
        Log.i(TAG, "getClearResult: ");
        return clearResult;
    }
    public void setClearResult(boolean clearResult) {
        Log.i(TAG, "setClearResult: ");
        this.clearResult = clearResult;
    }
    public String getAnswer() {
        Log.i(TAG, "getAnswer: ");
        return answer;
    }
    public void setAnswer(String answer) {
        Log.i(TAG, "setAnswer: ");
        this.answer = answer;
    }

    public boolean Solve(){
        Log.i(TAG, "Solve: ");
        if(input.split("\\*").length == 2) {
            String[] numbers = input.split("\\*");
            try {
                double mul = Double.parseDouble(numbers[0]) * Double.parseDouble(numbers[1]);
                input = mul + "";
            } catch (Exception e){
                error = e.toString();
                return false;
            }
        }
        else if(input.split("/").length == 2) {
            String[] numbers = input.split("/");
            try {
                double div = Double.parseDouble(numbers[0]) / Double.parseDouble(numbers[1]);
                input=div+"";
            } catch (Exception e){
                error = e.toString();
                return false;
            }
        } else if(input.split("\\^").length == 2) {
            String[] numbers = input.split("\\^");
            try {
                double pow=Math.pow(Double.parseDouble(numbers[0]), Double.parseDouble(numbers[1]));
                input = pow + "";
            } catch (Exception e) {
                error = e.toString();
                return false;
            }
        } else if(input.split("\\+").length == 2) {
            String[] numbers = input.split("\\+");
            try {
                double sum = Double.parseDouble(numbers[0]) + Double.parseDouble(numbers[1]);
                input = sum + "";
            } catch (Exception e) {
                error = e.toString();
                return false;
            }
        } else if(input.split("\\-").length > 1) {
            String[] numbers = input.split("\\-");
            if(numbers[0] == "" && numbers.length == 2) {
                numbers[0] = 0 + "";
            }
            try {
                double sub = 0;
                if(numbers.length == 2) {
                    sub = Double.parseDouble(numbers[0]) - Double.parseDouble(numbers[1]);
                } else if(numbers.length == 3){
                    sub = -Double.parseDouble(numbers[1]) - Double.parseDouble(numbers[2]);
                }
                input = sub + "";
            } catch (Exception e) {
                error = e.toString();
                return false;
            }
        }
        String[] n = input.split("\\.");
        if(n.length > 1) {
            if(n[1].equals("0")) {
                input = n[0];
            }
        }
        return true;
    }
}
