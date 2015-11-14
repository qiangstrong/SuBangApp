package com.subang.app.util;

import android.os.Bundle;
import android.os.Message;

/**
 * Created by Qiang on 2015/11/10.
 */
public class ComUtil {

    public static Message getMessage(int what,String info){
        Message msg = new Message();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("info", info);
        msg.setData(bundle);
        return msg;
    }

    public static String getInfo(Message msg){
        Bundle bundle=msg.getData();
        String info=bundle.getString("info");
        return info;
    }
}
