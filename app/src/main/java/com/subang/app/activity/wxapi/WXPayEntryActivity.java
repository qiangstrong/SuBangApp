package com.subang.app.activity.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.subang.app.activity.MainActivity;
import com.subang.app.activity.PayResultActivity;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.BasePrepayResult;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

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
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {            //支付成功
                Intent intent = null;

                if (appShare.map.containsKey("recharge")) {
                    appShare.map.remove("recharge");
                    appShare.map.put("main.position", 2);
                    appShare.map.put("mine.refresh", true);
                    intent = new Intent(WXPayEntryActivity.this, MainActivity.class);
                } else if (appShare.map.containsKey("pay")) {
                    appShare.map.remove("pay");
                    appShare.map.put("main.position", 1);
                    appShare.map.put("order.position", 0);
                    appShare.map.put("type.refresh", true);
                    intent = new Intent(WXPayEntryActivity.this, MainActivity.class);
                } else {
                    appShare.map.put("main.position", 0);
                    intent = new Intent(WXPayEntryActivity.this, MainActivity.class);
                }

                startActivity(intent);
            } else if (resp.errCode == BaseResp.ErrCode.ERR_COMM) {    //错误
                Intent intent = new Intent(WXPayEntryActivity.this, PayResultActivity.class);
                BasePrepayResult basePrepayResult = new BasePrepayResult();
                basePrepayResult.setCode(BasePrepayResult.Code.fail);
                basePrepayResult.setMsg("支付错误");
                intent.putExtra("payresult", basePrepayResult);
                startActivity(intent);
            } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {     //用户取消
                AppUtil.tip(WXPayEntryActivity.this, "取消支付");
            }
        }
        finish();
    }
}