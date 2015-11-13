package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.subang.api.RegionAPI;
import com.subang.app.helper.AddrDataHelper;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.applib.view.WheelView;
import com.subang.bean.AddrData;
import com.subang.domain.City;
import com.subang.domain.District;
import com.subang.domain.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaActivity extends Activity {

    private static final int WHAT_CITY = 1;
    private static final int WHAT_DISTRICT = 2;
    private static final int WHAT_REGION = 3;

    private WheelView wv_city, wv_district, wv_region;

    private Thread cityThread, districtThread, regionThread;
    private Map<Integer, List<District>> districtMap;
    private Map<Integer, List<Region>> regionMap;
    private City cityFilter;
    private District districtFilter;
    private Region regionFilter;
    private AddrData addrData;

    private boolean isCity = false;
    private boolean isDistrict = false;
    private boolean isRegion = false;

    private WheelView.OnItemSelectedListener cityListener = new WheelView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(int selectedIndex) {
            if (!isCity){
                return;
            }
            if (districtThread == null || !districtThread.isAlive()) {
                addrData.selectCity(selectedIndex);
                isDistrict = false;
                districtThread = new Thread(districtRunnable);
                districtThread.start();
            }
        }
    };

    private WheelView.OnItemSelectedListener districtListener = new WheelView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(int selectedIndex) {
            if (!isDistrict){
                return;
            }
            if (regionThread == null || !regionThread.isAlive()) {
                addrData.selectDistrict(selectedIndex);
                isRegion = false;
                regionThread = new Thread(regionRunnable);
                regionThread.start();
            }
        }
    };

    private WheelView.OnItemSelectedListener regionListener = new WheelView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(int selectedIndex) {
            if (!isRegion){
                return;
            }
            addrData.selectRegion(selectedIndex);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(AreaActivity.this);
                    break;
                }
                case WHAT_CITY: {
                    wv_city.setItems(AddrDataHelper.toCityList(addrData));
                    wv_city.setSelectedIndex(0);
                    isCity = true;
                }
                case WHAT_DISTRICT: {
                    wv_district.setItems(AddrDataHelper.toDistrictList(addrData));
                    wv_district.setSelectedIndex(0);
                    isDistrict = true;
                }
                case WHAT_REGION: {
                    wv_region.setItems(AddrDataHelper.toRegionList(addrData));
                    wv_region.setSelectedIndex(0);
                    isRegion = true;
                }
            }

        }
    };

    private Runnable cityRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AreaActivity.this);
            addrData.setCitys(RegionAPI.listCity(cityFilter));
            if (addrData.getCitys() == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectCity(0);

            if (!downloadDistricts(addrData.getDefaultCityid())) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectDistrict(0);

            if (!downloadRegions(addrData.getDefaultDistrictid())) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectRegion(0);
            handler.sendEmptyMessage(WHAT_CITY);
        }
    };

    private Runnable districtRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AreaActivity.this);

            if (!downloadDistricts(addrData.getDefaultCityid())) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectDistrict(0);

            if (!downloadRegions(addrData.getDefaultDistrictid())) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectRegion(0);

            handler.sendEmptyMessage(WHAT_DISTRICT);
        }
    };

    private Runnable regionRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AreaActivity.this);
            if (!downloadRegions(addrData.getDefaultDistrictid())) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            addrData.selectRegion(0);

            handler.sendEmptyMessage(WHAT_REGION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);
        findView();

        List<String> placeholder=new ArrayList<>();
        placeholder.add("加载中");
        wv_city.setOnItemSelectedListener(cityListener);
        wv_city.setItems(placeholder);
        wv_district.setOnItemSelectedListener(districtListener);
        wv_district.setItems(placeholder);
        wv_region.setOnItemSelectedListener(regionListener);
        wv_region.setItems(placeholder);

        districtMap = new HashMap<>();
        regionMap = new HashMap<>();

        cityFilter = new City();
        cityFilter.setId(0);
        cityFilter.setName("");
        districtFilter = new District();
        districtFilter.setId(0);
        districtFilter.setName("");
        regionFilter = new Region();
        regionFilter.setId(0);
        regionFilter.setName("");

        addrData = new AddrData();

        if (cityThread == null || !cityThread.isAlive()) {
            isCity = false;
            cityThread = new Thread(cityRunnable);
            cityThread.start();
        }
    }

    private void findView() {
        wv_city = (WheelView) findViewById(R.id.wv_city);
        wv_district = (WheelView) findViewById(R.id.wv_district);
        wv_region = (WheelView) findViewById(R.id.wv_region);
    }

    public void tv_cancel_onClick(View view) {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }

    public void tv_ok_onClick(View view) {
        //数据正在加载，前台数据已经过时
        if (!(isCity && isDistrict && isRegion)) {
            return;
        }
        Intent intent = getIntent();
        intent.putExtra("addrData", addrData);
        setResult(RESULT_OK, intent);
        finish();
    }

    //非UI线程执行代码
    private boolean downloadDistricts(int selectedCityid) {
        if (districtMap.containsKey(selectedCityid)) {
            addrData.setDistricts(districtMap.get(selectedCityid));
        } else {
            addrData.setDistricts(RegionAPI.listDistrict(selectedCityid, districtFilter));
            if (addrData.getDistricts() == null) {
                return false;
            }
            districtMap.put(selectedCityid, addrData.getDistricts());
        }
        return true;
    }

    //非UI线程执行代码
    private boolean downloadRegions(int selectedDistrictid) {
        if (regionMap.containsKey(selectedDistrictid)) {
            addrData.setRegions(regionMap.get(selectedDistrictid));
        } else {
            addrData.setRegions(RegionAPI.listRegion(selectedDistrictid, regionFilter));
            if (addrData.getRegions() == null) {
                return false;
            }
            regionMap.put(selectedDistrictid, addrData.getRegions());
        }
        return true;
    }
}
