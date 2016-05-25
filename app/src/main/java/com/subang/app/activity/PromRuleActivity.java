package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.subang.api.InfoAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.domain.Info;

public class PromRuleActivity extends Activity {

    private TextView tv_rule;

    private Thread thread;
    private Info info;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PromRuleActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    setRule();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PromRuleActivity.this);
            info= InfoAPI.get(null);
            if (info == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prom_rule);
        findView();
        initInfo();
        setRule();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView(){
        tv_rule=(TextView)findViewById(R.id.tv_rule);
    }

    private void initInfo(){
        info=new Info();
        info.setSalaryLimit(50.0);
        info.setProm0(10);
        info.setProm1(3);
        info.setProm2(2);
    }

    private void setRule(){
        tv_rule.setText(getString(R.string.prom_rule, info.getProm0(), info.getProm1(), info.getProm0(), info.getProm2(),info.getProm1(),info.getProm0(),info.getSalaryLimit()));
    }

    public void btn_ok_onClick(View view) {
        finish();
    }
}
