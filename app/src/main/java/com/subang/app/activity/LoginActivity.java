package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.Result;
import com.subang.domain.User;

public class LoginActivity extends Activity {

    private EditText et_cellnum, et_password;
    private TextView tv_login;

    private Thread thread;
    private User user;

    private boolean isCellnum = false;
    private boolean isPassword = false;

    private TextWatcher cellnumWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 11) {    //电话号码的长度
                isCellnum = true;
            } else {
                isCellnum = false;
            }
            prepare();
        }
    };

    private TextWatcher passwordWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                isPassword = true;
            } else {
                isPassword = false;
            }
            prepare();
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
                case 1: {
                    Toast toast = Toast.makeText(LoginActivity.this, "用户名或密码错误。", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                case 2: {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
            Result result = UserAPI.login(user);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);    //提示用户，停留此界面
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                handler.sendEmptyMessage(1);            //提示输入错误
                return;
            }
            AppUtil.saveConf(LoginActivity.this, user);
            AppUtil.conf(LoginActivity.this);
            AppUtil.confApi(LoginActivity.this);
            AppUtil.setLocation(LoginActivity.this);
            handler.sendEmptyMessage(2);                //转主界面
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        user=new User();
        et_cellnum.addTextChangedListener(cellnumWatcher);
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

    private void prepare() {
        if (isCellnum && isPassword) {
            tv_login.setEnabled(true);
        } else {
            tv_login.setEnabled(false);
        }
    }
}
