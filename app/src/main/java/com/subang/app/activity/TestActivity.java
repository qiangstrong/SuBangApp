package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class TestActivity extends Activity {


    private ListView lv_test;
    private List<String> items;

    AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e("Qiang",Integer.toString(position));
            Log.e("Qiang", lv_test.getChildCount()+"");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        lv_test=(ListView)findViewById(R.id.lv_test);
        createItems();
        lv_test.setAdapter(new ArrayAdapter<String>(TestActivity.this,
                android.R.layout.simple_list_item_1, items));
        lv_test.setOnItemClickListener(onItemClickListener);
    }

    public void onClick(View view){

    }

    private void createItems() {
        items=new ArrayList<>();
        for (int i=0;i<2;i++){
            items.add(i+"");
        }
    }


}
