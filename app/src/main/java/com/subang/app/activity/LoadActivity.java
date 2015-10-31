package com.subang.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.subang.api.SubangAPI;
import com.subang.api.UserAPI;
import com.subang.app.util.AppUtil;
import com.subang.bean.Result;
import com.subang.domain.User;
import com.subang.util.WebConst;

import java.util.List;

public class LoadActivity extends Activity {


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    Toast toast = Toast.makeText(LoadActivity.this, R.string.err_network, Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                case 2: {
                    Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
                case 3: {
                    Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!AppUtil.checkNetwork(LoadActivity.this)) {
                handler.sendEmptyMessage(1);    //提示用户，停留此界面
                return;
            }
            User user = AppUtil.readUser(LoadActivity.this);
            if (user == null) {
                handler.sendEmptyMessage(2);    //转登录界面
                return;
            }
            if (!login(user)) {
                handler.sendEmptyMessage(2);    //转登录界面
                return;
            }
            setLocation();
            handler.sendEmptyMessage(3);        //转主界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        new Thread(runnable).start();
    }



    private boolean login(User user) {
        Result result = UserAPI.login(user);
        if (result == null || !result.getCode().equals(Result.OK)) {
            return false;
        }
        String basePath = getFilesDir().getAbsolutePath() + "/";
        SubangAPI.conf(WebConst.USER, user.getCellnum(), user.getPassword(), basePath);
        return true;
    }

    private boolean setLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = locationManager.getBestProvider(new Criteria(), true);
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(bestProvider);
            if (location == null) {
                List<String> providers = locationManager.getAllProviders();
                for (String provider : providers) {
                    location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        break;
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (location == null) {
            return false;
        }
        com.subang.domain.Location myLocation = new com.subang.domain.Location();
        myLocation.setLatitude(Double.toString(location.getLatitude()));
        myLocation.setLongitude(Double.toString(location.getLongitude()));
        Result result = UserAPI.setLocation(myLocation);
        if (!result.getCode().equals(Result.OK)) {
            return false;
        }
        return true;
    }
}
