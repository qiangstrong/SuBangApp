package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.AliPayResult;
import com.subang.bean.BasePrepayResult;

public class AliPrepayActivity extends Activity {

    private AppShare appShare;
    private String payRequest;
    private AliPayResult payResult;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (payResult.getResultStatus().equals("9000")) {            //支付成功
                        Intent intent = null;
                        if (appShare.map.containsKey("recharge")) {
                            appShare.map.remove("recharge");
                            appShare.map.put("main.position", 2);
                            appShare.map.put("mine.refresh", true);
                            intent = new Intent(AliPrepayActivity.this, MainActivity.class);
                        } else if (appShare.map.containsKey("pay")) {
                            appShare.map.remove("pay");
                            appShare.map.put("main.position", 1);
                            appShare.map.put("order.position", 0);
                            appShare.map.put("type.refresh", true);
                            intent = new Intent(AliPrepayActivity.this, MainActivity.class);
                        } else {
                            appShare.map.put("main.position", 0);
                            intent = new Intent(AliPrepayActivity.this, MainActivity.class);
                        }
                        startActivity(intent);
                    } else if (payResult.getResultStatus().equals("6001")) {    //用户取消
                        AppUtil.tip(AliPrepayActivity.this, "取消支付");
                    } else  {                                                   //错误
                        Intent intent = new Intent(AliPrepayActivity.this, PayResultActivity.class);
                        BasePrepayResult basePrepayResult = new BasePrepayResult();
                        basePrepayResult.setCode(BasePrepayResult.Code.fail);
                        basePrepayResult.setMsg("支付错误");
                        intent.putExtra("payresult", basePrepayResult);
                        startActivity(intent);
                    }
                    break;
                }
            }
            finish();
        }
    };


    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            PayTask alipay = new PayTask(AliPrepayActivity.this);
            String result = alipay.pay(payRequest);
            payResult=new AliPayResult(result);
            Message msg = new Message();
            msg.what = AppConst.WHAT_SUCC_SUBMIT;
            handler.sendMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        payRequest = (String) getIntent().getSerializableExtra("payrequest");
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
