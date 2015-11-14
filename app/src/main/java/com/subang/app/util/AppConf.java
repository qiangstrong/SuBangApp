package com.subang.app.util;

/**
 * Created by Qiang on 2015/11/3.
 */
public class AppConf {
    public static String basePath;
    public static String cellnum;
    public static String password;

    public static boolean isConfed() {
        if (cellnum == null || password == null || basePath == null) {
            return false;
        }
        return true;
    }

    public static void invalidate() {
        cellnum = null;
        password = null;
        basePath = null;
    }
}
