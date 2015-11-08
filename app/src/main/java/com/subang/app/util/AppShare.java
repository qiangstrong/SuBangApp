package com.subang.app.util;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Qiang on 2015/11/7.
 */
public class AppShare extends Application {
    public Map<String, Object> map;

    @Override
    public void onCreate() {
        super.onCreate();
        map = new HashMap<>();
    }
}
