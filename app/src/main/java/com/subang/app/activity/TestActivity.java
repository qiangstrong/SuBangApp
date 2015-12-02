package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;


public class TestActivity extends Activity {

    private RatingBar rb_test;
    private ImageView iv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        rb_test=(RatingBar)findViewById(R.id.rb_test);
    }

    public void onClick(View view){
        Log.e("Qiang",rb_test.getProgress()+"");
    }

    private void createItems() {

    }


}
