package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.subang.applib.view.WheelView;

import java.util.Arrays;


public class TestActivity extends Activity {


    private String[] main = new String[]{"1","2"};
    private String[] PLANETS = new String[]{"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Uranus", "Neptune", "Pluto"};
    private String[] PLANETS1 = new String[]{"Mercury1", "Venus1", "Earth1", "Mars1", "Jupiter1", "Uranus1", "Neptune1", "Pluto1"};

    private WheelView wv_1,wv_2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        wv_1=(WheelView)findViewById(R.id.wv_1);
        wv_2=(WheelView)findViewById(R.id.wv_2);

        wv_1.setOffset(3);
        wv_1.setItems(Arrays.asList(main));
        wv_1.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int selectedIndex) {
                if (selectedIndex == 0) {
                    wv_2.setItems(Arrays.asList(PLANETS));
                } else {
                    wv_2.setItems(Arrays.asList(PLANETS1));
                }
            }
        });

        wv_2.setOffset(3);
        wv_2.setItems(Arrays.asList(PLANETS));
        wv_2.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int selectedIndex) {
                if (wv_1.getSelectedIndex()==0) {
                    Log.e("Qiang", PLANETS[selectedIndex]);
                } else {
                    Log.e("Qiang", PLANETS1[selectedIndex]);
                }
            }
        });

    }

    public void onClick(View view){
        wv_1.setItems(Arrays.asList(PLANETS1));
    }

    private void createItems() {

    }


}
