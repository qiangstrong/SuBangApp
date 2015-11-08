package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.subang.api.UserAPI;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.Result;
import com.subang.domain.User;

public class LoadActivity extends Activity {


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(LoadActivity.this);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
                case 2: {
                    Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!AppUtil.checkNetwork(LoadActivity.this)) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }

            if (!AppUtil.conf(LoadActivity.this)) {
                handler.sendEmptyMessage(1);    //转登录界面
                return;
            }
            User user = new User();
            user.setCellnum(AppConf.cellnum);
            user.setPassword(AppConf.password);
            Result result = UserAPI.login(user);
            if (result==null){
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                handler.sendEmptyMessage(1);    //转登录界面
                return;
            }
            AppUtil.confApi(LoadActivity.this);
            AppUtil.setLocation(LoadActivity.this);
            handler.sendEmptyMessage(2);        //转主界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        new Thread(runnable).start();
    }
}
