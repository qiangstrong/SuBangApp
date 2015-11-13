package com.subang.app.helper;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Qiang on 2015/11/10.
 */
public class MyTextWatcher implements TextWatcher {

    private boolean isAvail = false;
    private int minLength;
    private OnPrepareListener listener;

    public MyTextWatcher() {
    }

    public MyTextWatcher(int minLength, OnPrepareListener listener) {
        this.minLength = minLength;
        this.listener = listener;
    }

    public boolean isAvail() {
        return isAvail;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public OnPrepareListener getListener() {
        return listener;
    }

    public void setListener(OnPrepareListener listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() >= minLength) {
            isAvail = true;
        } else {
            isAvail = false;
        }
        listener.onPrepare();
    }

    public interface OnPrepareListener {
        void onPrepare();
    }
}
