package com.subang.app.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.subang.app.util.AppConst;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeixinReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI wxapi = WXAPIFactory.createWXAPI(context, null);

        wxapi.registerApp(AppConst.APP_ID);
    }
}
