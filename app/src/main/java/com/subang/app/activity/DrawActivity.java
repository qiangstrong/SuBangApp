package com.subang.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.subang.api.InfoAPI;
import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.subang.domain.Info;

public class DrawActivity extends Activity {

    private AppShare appShare;

    private TextView tv_rule;
    private ProgressDialog progressDialog;

    private Thread thread, submitThread;
    private Info info;

    private AlertDialog.OnClickListener dialogOnClickListener = new AlertDialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                appShare.map.put("main.position", 2);
                appShare.map.put("mine.refresh", true);
                Intent intent = new Intent(DrawActivity.this, MainActivity.class);
                startActivity(intent);
                DrawActivity.this.finish();
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    AppUtil.networkTip(DrawActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    setRule();
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    new AlertDialog.Builder(DrawActivity.this)
                            .setMessage(ComUtil.getInfo(msg))
                            .setPositiveButton("确定", dialogOnClickListener)
                            .create().show();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(DrawActivity.this);
            info = InfoAPI.get(null);
            if (info == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(DrawActivity.this);
            Result result = UserAPI.draw();
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (result.isOk()){
                result.setMsg("提现成功。收益已转入您的微信账户。");
            }
            Message msg= ComUtil.getMessage(AppConst.WHAT_SUCC_SUBMIT,result.getMsg());
            handler.sendMessage(msg);            //到订单列表界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        appShare = (AppShare) getApplication();
        findView();
        initInfo();
        setRule();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        tv_rule = (TextView) findViewById(R.id.tv_rule);
    }

    private void initInfo() {
        info = new Info();
        info.setSalaryLimit(40.0);
    }

    private void setRule() {
        tv_rule.setText(getString(R.string.draw_rule, info.getSalaryLimit()));
    }

    public void btn_draw_onClick(View view) {
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
        progressDialog = ProgressDialog.show(this, "提示", "正在提现...");
    }

    public void iv_back_onClick(View view) {
        finish();
    }
}