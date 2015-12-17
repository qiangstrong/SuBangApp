package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.subang.api.UserAPI;
import com.subang.app.helper.MyTextWatcher;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.subang.domain.User;
import com.subang.util.WebConst;

import java.util.Map;

public class PasswordActivity extends Activity {

    private static final int WHAT_SIGNIN = 1;
    private static final int WHAT_CHANGE = 2;

    private int type;       //标志此activity用于改变用户信息，还是用于注册

    private EditText et_password1, et_password2;
    private TextView tv_ok;
    private LinearLayout ll_term;

    private Thread signinThread, changeThread;
    private User user;
    private String password;

    private MyTextWatcher password1Watcher, password2Watcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (password1Watcher.isAvail() && password2Watcher.isAvail()) {
                tv_ok.setEnabled(true);
            } else {
                tv_ok.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PasswordActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(PasswordActivity.this, info);
                    break;
                }
                case WHAT_SIGNIN: {
                    Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
                case WHAT_CHANGE: {
                    AppUtil.tip(PasswordActivity.this, "密码更改成功。");
                    PasswordActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable signinRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PasswordActivity.this);
            user.setPassword(password);
            Map<String, String> errors = UserAPI.add(user);
            if (errors == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (!errors.isEmpty()) {
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "该手机号已被注册。");
                handler.sendMessage(msg);
                return;
            }
            AppUtil.saveConf(PasswordActivity.this, user);
            AppUtil.conf(PasswordActivity.this);
            AppUtil.confApi(PasswordActivity.this);
            handler.sendEmptyMessage(WHAT_SIGNIN);
            AppUtil.setLocation(PasswordActivity.this);
        }
    };

    private Runnable changeRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PasswordActivity.this);
            Result result = UserAPI.chgPassword(password);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            AppUtil.conf(PasswordActivity.this);
            User user = new User();
            user.setCellnum(AppConf.cellnum);
            user.setPassword(password);
            AppUtil.saveConf(PasswordActivity.this, user);
            AppUtil.conf(PasswordActivity.this);
            AppUtil.confApi(PasswordActivity.this);
            handler.sendEmptyMessage(WHAT_CHANGE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", AppConst.TYPE_SIGNIN);
        if (type == AppConst.TYPE_SIGNIN) {
            user = new User();
            user.setCellnum(getIntent().getStringExtra("cellnum"));
        }
        setContentView(R.layout.activity_password);
        findView();
        password1Watcher = new MyTextWatcher(1, onPrepareListener);
        et_password1.addTextChangedListener(password1Watcher);
        password2Watcher = new MyTextWatcher(1, onPrepareListener);
        et_password2.addTextChangedListener(password2Watcher);

        if (type==AppConst.TYPE_SIGNIN){
            ll_term.setVisibility(View.VISIBLE);
        }
    }

    private void findView() {
        et_password1 = (EditText) findViewById(R.id.et_password1);
        et_password2 = (EditText) findViewById(R.id.et_password2);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        ll_term=(LinearLayout)findViewById(R.id.ll_term);
    }

    public void tv_ok_onClick(View view) {
        String password1 = et_password1.getText().toString();
        String password2 = et_password2.getText().toString();
        if (!password1.equals(password2)) {
            AppUtil.tip(PasswordActivity.this, "两次输入密码不一致。");
            return;
        }
        password = password1;
        if (type == AppConst.TYPE_SIGNIN) {
            if (signinThread == null || !signinThread.isAlive()) {
                signinThread = new Thread(signinRunnable);
                signinThread.start();
            }
        } else {
            if (changeThread == null || !changeThread.isAlive()) {
                changeThread = new Thread(changeRunnable);
                changeThread.start();
            }
        }
    }

    public void tv_term_onClick(View view){
        Intent intent = new Intent(PasswordActivity.this, WebActivity.class);
        intent.putExtra("title", "用户协议");
        intent.putExtra("url", WebConst.HOST_URI + "content/weixin/info/term.htm");
        startActivity(intent);
    }
}
