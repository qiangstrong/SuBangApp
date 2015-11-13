package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.subang.api.RegionAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.domain.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityActivity extends Activity {

    private ListView lv_city;

    private SimpleAdapter citySimpleAdapter;

    private Thread thread;
    private List<City> citys;
    private List<Map<String, Object>> cityItems;

    private boolean isLoaded = false;

    private AdapterView.OnItemClickListener cityOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!isLoaded){
                return;
            }
            Intent intent=getIntent();
            intent.putExtra("city",citys.get(position));
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(CityActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    cityItems.clear();
                    Map<String, Object> cityItem;
                    for (City city : citys) {
                        cityItem = new HashMap<>();
                        cityItem.put("name", city.getName());
                        cityItems.add(cityItem);
                    }
                    citySimpleAdapter.notifyDataSetChanged();
                    isLoaded = true;
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(CityActivity.this);
            City filter = new City();
            filter.setId(0);
            filter.setName("");
            citys = RegionAPI.listCity(filter);
            if (citys == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        findView();
        cityItems = new ArrayList<>();
        citySimpleAdapter = new SimpleAdapter(CityActivity.this, cityItems, R.layout.item_city, new
                String[]{"name"}, new int[]{R.id.tv_name});
        lv_city.setAdapter(citySimpleAdapter);
        lv_city.setOnItemClickListener(cityOnItemClickListener);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        lv_city = (ListView) findViewById(R.id.lv_city);
    }

    public void iv_back_onClick(View view) {
        finish();
    }
}
