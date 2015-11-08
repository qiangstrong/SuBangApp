package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class TestActivity extends Activity {




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

    }

    public void onClick(View view){
        Log.e("Qiang",view.getClass().getName());
    }

    private void createItems() {

    }
}
