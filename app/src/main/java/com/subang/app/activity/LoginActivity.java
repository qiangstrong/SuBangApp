package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.subang.api.UserAPI;
import com.subang.app.helper.MyTextWatcher;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.subang.domain.User;

public class LoginActivity extends Activity {

    private static final int WHAT_MAIN = 1;

    private EditText et_cellnum, et_password;
    private TextView tv_login;

    private Thread thread;
    private User user;

    private MyTextWatcher cellnumWatcher, passwordWatcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (cellnumWatcher.isAvail() && passwordWatcher.isAvail()) {
                tv_login.setEnabled(true);
            } else {
                tv_login.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(LoginActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(LoginActivity.this, info);
                    break;
                }
                case WHAT_MAIN: {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    LoginActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(LoginActivity.this);
            Result result = UserAPI.login(user);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "用户名或密码错误。");
                handler.sendMessage(msg);
                return;
            }
            AppUtil.saveConf(LoginActivity.this, user);
            AppUtil.conf(LoginActivity.this);
            AppUtil.confApi(LoginActivity.this);
            handler.sendEmptyMessage(WHAT_MAIN);                //转主界面
            AppUtil.setLocation(LoginActivity.this);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        user = new User();

        int cellnumLength = getResources().getInteger(R.integer.cellnum);
        cellnumWatcher = new MyTextWatcher(cellnumLength, onPrepareListener);
        et_cellnum.addTextChangedListener(cellnumWatcher);
        passwordWatcher = new MyTextWatcher(1, onPrepareListener);
        et_password.addTextChangedListener(passwordWatcher);
    }

    private void findView() {
        et_cellnum = (EditText) findViewById(R.id.et_cellnum);
        et_password = (EditText) findViewById(R.id.et_password);
        tv_login = (TextView) findViewById(R.id.tv_login);
    }

    public void tv_login_onClick(View view) {
        user.setCellnum(et_cellnum.getText().toString());
        user.setPassword(et_password.getText().toString());
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    public void tv_signin_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, CellnumActivity.class);
        intent.putExtra("type", AppConst.TYPE_SIGNIN);
        startActivity(intent);
    }

    public void tv_forget_onClick(View view){
        Intent intent = new Intent(LoginActivity.this, CellnumActivity.class);
        intent.putExtra("type", AppConst.TYPE_LOGIN);
        startActivity(intent);
    }
}
