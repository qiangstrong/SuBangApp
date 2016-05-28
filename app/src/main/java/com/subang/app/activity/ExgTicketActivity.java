package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.Result;

public class ExgTicketActivity extends Activity {

    private AppShare appShare;

    private EditText et_codeno;
    private TextView tv_tip;

    private Thread thread;
    private String codeno;
    private Result result;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(ExgTicketActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (result.isOk()) {
                        AppUtil.tip(ExgTicketActivity.this, "兑换成功");
                        appShare.map.put("ticket.refresh", true);
                        ExgTicketActivity.this.finish();
                    } else {
                        tv_tip.setText(result.getMsg());
                    }
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(ExgTicketActivity.this);
            result = UserAPI.exgTicket(codeno);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        setContentView(R.layout.activity_exg_ticket);
        findView();
    }

    private void findView() {
        et_codeno = (EditText) findViewById(R.id.et_codeno);
        tv_tip = (TextView) findViewById(R.id.tv_tip);
    }

    public void btn_cancle_onClick(View view) {
        finish();
    }

    public void btn_ok_onClick(View view) {
        codeno = et_codeno.getText().toString();
        if (codeno.length() == 0) {
            tv_tip.setText("请输入优惠码");
            return;
        }
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }
}
