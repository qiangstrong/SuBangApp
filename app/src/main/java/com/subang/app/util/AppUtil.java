package com.subang.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.subang.api.SubangAPI;
import com.subang.app.activity.R;
import com.subang.util.WebConst;

/**
 * Created by Qiang on 2015/10/31.
 */
public class AppUtil {
    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    public static boolean conf(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_user), context.MODE_PRIVATE);
        String basePath = context.getFilesDir().getAbsolutePath() + "/";
        String cellnum = sharedPreferences.getString("cellnum", null);
        String password = sharedPreferences.getString("password", null);
        if (cellnum != null && password != null) {
            AppConf.basePath=basePath;
            AppConf.cellnum=cellnum;
            AppConf.password=password;
            return true;
        }
        return false;
    }

    public static void confApi() {
        if (SubangAPI.isConfed()) {
            return;
        }
        SubangAPI.conf(WebConst.USER, AppConf.cellnum, AppConf.password, AppConf.basePath);
    }
}
