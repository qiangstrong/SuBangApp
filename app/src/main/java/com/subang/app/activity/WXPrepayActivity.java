package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;

import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.WeixinPayRequest;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPrepayActivity extends Activity {

    private IWXAPI wxapi;
    private WeixinPayRequest payRequest;
    private PayReq payReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wxapi = WXAPIFactory.createWXAPI(this, AppConst.APP_ID, true);
        payRequest = (WeixinPayRequest) getIntent().getSerializableExtra("payrequest");
        if (!wxapi.registerApp(AppConst.APP_ID)) {
            AppUtil.tip(WXPrepayActivity.this, "没有找到微信客户端。");
            this.finish();
            return;
        }
        genPayReq();
        wxapi.sendReq(payReq);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void genPayReq() {
        payReq = new PayReq();
        payReq.appId = payRequest.getAppid();
        payReq.partnerId = payRequest.getPartnerid();
        payReq.prepayId = payRequest.getPrepayid();
        payReq.packageValue = payRequest.getPackage_();
        payReq.nonceStr = payRequest.getNoncestr();
        payReq.timeStamp = payRequest.getTimestamp();
        payReq.sign = payRequest.getSign();
    }
}
