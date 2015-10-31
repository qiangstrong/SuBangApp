package com.subang.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.subang.api.SubangAPI;
import com.subang.app.activity.R;
import com.subang.domain.User;
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

    public static User readUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string
                .file_user), context.MODE_PRIVATE);
        String cellnum = sharedPreferences.getString("cellnum", null);
        String password = sharedPreferences.getString("password", null);
        if (cellnum != null && password != null) {
            User user = new User();
            user.setCellnum(cellnum);
            user.setPassword(password);
            return user;
        }
        return null;
    }

    public static void confApi(Context context) {
        if (SubangAPI.isConfed()) {
            return;
        }
        User user = readUser(context);
        if (user == null) {
            return;         //应该不会出现这种情况
        }
        String basePath = context.getFilesDir().getAbsolutePath() + "/";
        SubangAPI.conf(WebConst.USER, user.getCellnum(), user.getPassword(), basePath);
    }
}
