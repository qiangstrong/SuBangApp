package com.subang.app.util;

import android.os.Message;
import android.util.Log;

/**
 * Created by Qiang on 2015/11/10.
 */
public class ComUtil {

    public static Message getMessage(int what,String info){
        Message msg = new Message();
        msg.what = what;
        msg.obj=info;
        return msg;
    }

    public static String getInfo(Message msg){
        return (String)msg.obj;
    }

    public static void log(String code,String msg){
        Log.e(AppConst.LOG_TAG,"错误码:" + code+ "; 错误信息:" + msg);
    }
}
