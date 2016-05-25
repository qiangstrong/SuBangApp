package com.subang.app.activity.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private AppShare appShare;
    private IWXAPI wxapi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        wxapi = WXAPIFactory.createWXAPI(this,AppConst.APP_ID, true);
        wxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                if (appShare.map.containsKey(resp.transaction)){
                    Integer orderid=(Integer)appShare.map.get(resp.transaction);
                    appShare.map.remove(resp.transaction);
                    share(orderid);
                }
            }
        }else {
            ComUtil.log(String.valueOf(resp.getType()), String.valueOf(resp.errCode));
        }
        finish();
    }

    //分享成功之后返现
    private void share(final Integer orderid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppUtil.confApi(WXEntryActivity.this);
                Result result=UserAPI.share(orderid);
            }
        }).start();
    }
}