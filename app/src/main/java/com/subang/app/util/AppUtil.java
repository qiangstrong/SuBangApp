package com.subang.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.subang.api.SubangAPI;
import com.subang.api.UserAPI;
import com.subang.app.activity.R;
import com.subang.bean.Result;
import com.subang.domain.User;
import com.subang.util.WebConst;

import java.util.List;

/**
 * Created by Qiang on 2015/10/31.
 */
public class AppUtil {

    public static boolean isConfed() {
        if (AppConf.cellnum == null || AppConf.password == null || AppConf.basePath == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void saveConf(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_user), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cellnum", user.getCellnum());
        editor.putString("password", user.getPassword());
        editor.commit();
    }

    public static boolean conf(Context context) {
        if (isConfed()) {
            return true;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_user), Context.MODE_PRIVATE);
        String basePath = context.getFilesDir().getAbsolutePath() + "/";
        String cellnum = sharedPreferences.getString("cellnum", null);
        String password = sharedPreferences.getString("password", null);
        if (cellnum != null && password != null) {
            AppConf.basePath = basePath;
            AppConf.cellnum = cellnum;
            AppConf.password = password;
            return true;
        }
        return false;
    }

    public static void confApi(Context context) {
        if (SubangAPI.isConfed()) {
            return;
        }
        conf(context);
        SubangAPI.conf(WebConst.USER, AppConf.cellnum, AppConf.password, AppConf.basePath);
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    public static void networkTip(Context context) {
        Toast toast = Toast.makeText(context, R.string.err_network, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void updateTip(Context context) {
        Toast toast = Toast.makeText(context, R.string.err_update, Toast.LENGTH_LONG);
        toast.show();
    }

    //后台执行
    public static boolean setLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
