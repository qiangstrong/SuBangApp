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
import com.subang.util.SuUtil;
import com.subang.util.WebConst;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends Activity {

    private static final int WHAT_GET_SUCC = 1;
    private static final int WHAT_GET_FAIL = 2;
    private static final int WHAT_OK = 3;

    private int type;       //标志此activity用于改变用户信息，注册, 登录
    private Timer timer;    //调度timerTask

    private EditText et_cellnum, et_authcode;
    private TextView tv_get, tv_ok;
    private LinearLayout ll_term;

    private Thread getThread, okThread;
    private String cellnum, authcode;
    private int downCounter;            //tv_get按钮的倒计时计数器
    private TimerTask timerTask;        //5分钟后取消过期的验证码

    private MyTextWatcher cellnumWatcher, authcodeWatcher;
    private boolean isCellnumWatcher = true;             //tv_get按钮的状态是否由cellnumWatcher控制

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (isCellnumWatcher) {
                if (cellnumWatcher.isAvail()) {
                    tv_get.setEnabled(true);
                } else {
                    tv_get.setEnabled(false);
                }
            }
            if (cellnumWatcher.isAvail() && authcodeWatcher.isAvail()) {
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
                    AppUtil.networkTip(LoginActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(LoginActivity.this, info);
                    break;
                }
                case WHAT_GET_FAIL: {
                    tv_get.setText("获取验证码");
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(LoginActivity.this, info);
                    break;
                }
                case WHAT_GET_SUCC: {
                    isCellnumWatcher = false;
                    tv_get.setClickable(false);
                    tv_get.setText(WebConst.AUTHCODE_NEXT_INTERVAL + "s");
                    downCounter = WebConst.AUTHCODE_NEXT_INTERVAL;
                    handler.postDelayed(textViewRunnable, WebConst.ONE_SECOND);
                    break;
                }
                case WHAT_OK: {
                    if (type == AppConst.TYPE_CHANGE) {
                        AppUtil.tip(LoginActivity.this, "手机号更改成功。");
                        LoginActivity.this.finish();
                    } else if (type == AppConst.TYPE_LOGIN) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }
                    break;
                }
            }
        }
    };

    //tv_get倒计时，更新界面
    private Runnable textViewRunnable = new Runnable() {
        @Override
        public void run() {
            downCounter--;
            if (downCounter == 0) {
                tv_get.setText("获取验证码");
                isCellnumWatcher = true;
                onPrepareListener.onPrepare();
                tv_get.setClickable(true);
            } else {
                tv_get.setText(downCounter + "s");
                handler.postDelayed(textViewRunnable, WebConst.ONE_SECOND);
            }
        }
    };

    private Runnable getRunnable = new Runnable() {
        @Override
        public void run() {
            Result result = null;
            AppUtil.confApi(LoginActivity.this);
            cellnum = et_cellnum.getText().toString();
            if (type == AppConst.TYPE_CHANGE) {
                result = UserAPI.chkCellnum(et_cellnum.getText().toString());
                if (result == null) {
                    handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                    return;
                }
                if (!result.isOk()) {
                    Message msg = ComUtil.getMessage(WHAT_GET_FAIL, "该手机号已被注册。");
                    handler.sendMessage(msg);
                    return;
                }
            }
            if (timerTask != null) {
                timerTask.cancel();
            }
            authcode = SuUtil.getAuthcode();
            result = UserAPI.getAuthcode(cellnum, authcode);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (!result.isOk()) {
                authcode = null;
                Message msg = ComUtil.getMessage(WHAT_GET_FAIL, "发送验证码错误。");
                handler.sendMessage(msg);
                return;
            }
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    authcode = null;
                }
            };
            timer.schedule(timerTask, WebConst.AUTHCODE_INTERVAL);
            handler.sendEmptyMessage(WHAT_GET_SUCC);
        }
    };

    private Runnable okRunnable = new Runnable() {
        @Override
        public void run() {
            Result result = null;
            AppUtil.confApi(LoginActivity.this);
            if (type == AppConst.TYPE_CHANGE) {
                result = UserAPI.chgCellnum(cellnum);
                if (result == null) {
                    handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                    return;
                }
                if (!result.isOk()) {
                    Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "该手机号已被注册。");
                    handler.sendMessage(msg);
                    return;
                }

                AppUtil.conf(LoginActivity.this);
                User user = new User();
                user.setCellnum(cellnum);
                user.setPassword(AppConf.password);
                AppUtil.saveConf(LoginActivity.this, user);
                AppUtil.conf(LoginActivity.this);
                AppUtil.confApi(LoginActivity.this);
                handler.sendEmptyMessage(WHAT_OK);
            } else if (type == AppConst.TYPE_LOGIN) {
                result = UserAPI.login(cellnum);
                if (result == null) {
                    handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                    return;
                }
                if (!result.isOk()) {
                    Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "登录失败");
                    handler.sendMessage(msg);
                    return;
                }
                User user=new User();
                user.setCellnum(cellnum);
                AppUtil.saveConf(LoginActivity.this, user);
                AppUtil.conf(LoginActivity.this);
                AppUtil.confApi(LoginActivity.this);
                handler.sendEmptyMessage(WHAT_OK);                //转主界面
                AppUtil.setLocation(LoginActivity.this);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", AppConst.TYPE_LOGIN);
        setContentView(R.layout.activity_lgoin);
        findView();

        int cellnumLength = getResources().getInteger(R.integer.cellnum);
        int authcodeLength = getResources().getInteger(R.integer.authcode);
        cellnumWatcher = new MyTextWatcher(cellnumLength, onPrepareListener);
        et_cellnum.addTextChangedListener(cellnumWatcher);
        authcodeWatcher = new MyTextWatcher(authcodeLength, onPrepareListener);
        et_authcode.addTextChangedListener(authcodeWatcher);

        if (type==AppConst.TYPE_CHANGE){
            ll_term.setVisibility(View.INVISIBLE);
        }

        timer = new Timer();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void findView() {
        et_cellnum = (EditText) findViewById(R.id.et_cellnum);
        et_authcode = (EditText) findViewById(R.id.et_authcode);
        tv_get = (TextView) findViewById(R.id.tv_get);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        ll_term = (LinearLayout) findViewById(R.id.ll_term);
    }

    public void tv_get_onClick(View view) {
        if (getThread == null || !getThread.isAlive()) {
            tv_get.setText("请稍等");
            getThread = new Thread(getRunnable);
            getThread.start();
        }
    }

    public void tv_ok_onClick(View view) {
        if (authcode == null) {
            AppUtil.tip(LoginActivity.this, "您还没有获取验证码或验证码已经失效。");
            return;
        }
        String authcode_new = et_authcode.getText().toString();
        if (!authcode.equals(authcode_new)) {
            AppUtil.tip(LoginActivity.this, "验证码输入错误。");
            return;
        }
        if (okThread == null || !okThread.isAlive()) {
            okThread = new Thread(okRunnable);
            okThread.start();
        }
    }

    public void tv_term_onClick(View view){
        Intent intent = new Intent(LoginActivity.this, WebActivity.class);
        intent.putExtra("title", "用户协议");
        intent.putExtra("url", WebConst.HOST_URI + "content/weixin/info/term.htm");
        startActivity(intent);
    }
}
