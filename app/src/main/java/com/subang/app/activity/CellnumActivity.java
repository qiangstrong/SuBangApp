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
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.subang.domain.User;
import com.subang.util.SmsUtil;
import com.subang.util.SuUtil;
import com.subang.util.WebConst;

import java.util.Timer;
import java.util.TimerTask;

public class CellnumActivity extends Activity {

    private static final int WHAT_GET = 1;
    private static final int WHAT_OK = 2;

    private int type;       //标志此activity用于改变用户信息，还是用于注册
    private Timer timer;    //调度timerTask

    private EditText et_cellnum, et_authcode;
    private TextView tv_get, tv_ok;

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
                    AppUtil.networkTip(CellnumActivity.this);
                    break;
                }
                case AppConst.WHAT_INFO: {
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(CellnumActivity.this, info);
                    break;
                }
                case WHAT_GET: {
                    isCellnumWatcher = false;
                    tv_get.setClickable(false);
                    tv_get.setText("60s");
                    downCounter = WebConst.AUTHCODE_NEXT_INTERVAL;
                    handler.postDelayed(textViewRunnable, WebConst.ONE_SECOND);
                    break;
                }
                case WHAT_OK: {
                    AppUtil.tip(CellnumActivity.this, "手机号更改成功。");
                    CellnumActivity.this.finish();
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
            AppUtil.confApi(CellnumActivity.this);
            cellnum = et_cellnum.getText().toString();
            Result result = UserAPI.chkCellnum(et_cellnum.getText().toString());
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "该手机号已被注册。");
                handler.sendMessage(msg);
                return;
            }
            if (timerTask != null) {
                timerTask.cancel();
            }
            authcode = SuUtil.getUserAuthcode();
            if (!SmsUtil.send(cellnum, com.subang.util.AppConst.templateId_authcode, SmsUtil.toUserContent(authcode))) {
                authcode = null;
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "发送验证码错误。");
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
            handler.sendEmptyMessage(WHAT_GET);
        }
    };

    private Runnable okRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(CellnumActivity.this);
            Result result = UserAPI.chgCellnum(cellnum);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (!result.getCode().equals(Result.OK)) {
                Message msg = ComUtil.getMessage(AppConst.WHAT_INFO, "该手机号已被注册。");
                handler.sendMessage(msg);
                return;
            }

            AppUtil.conf(CellnumActivity.this);
            User user = new User();
            user.setCellnum(cellnum);
            user.setPassword(AppConf.password);
            AppUtil.saveConf(CellnumActivity.this, user);
            AppUtil.conf(CellnumActivity.this);
            AppUtil.confApi(CellnumActivity.this);
            handler.sendEmptyMessage(WHAT_OK);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", AppConst.TYPE_SIGNIN);
        setContentView(R.layout.activity_cellnum);
        findView();

        int cellnumLength = getResources().getInteger(R.integer.cellnum);
        int authcodeLength = getResources().getInteger(R.integer.authcode);
        cellnumWatcher = new MyTextWatcher(cellnumLength, onPrepareListener);
        et_cellnum.addTextChangedListener(cellnumWatcher);
        authcodeWatcher = new MyTextWatcher(authcodeLength, onPrepareListener);
        et_authcode.addTextChangedListener(authcodeWatcher);

        timer = new Timer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SmsUtil.init();
    }

    private void findView() {
        et_cellnum = (EditText) findViewById(R.id.et_cellnum);
        et_authcode = (EditText) findViewById(R.id.et_authcode);
        tv_get = (TextView) findViewById(R.id.tv_get);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
    }

    public void tv_get_onClick(View view) {
        if (getThread == null || !getThread.isAlive()) {
            getThread = new Thread(getRunnable);
            getThread.start();
        }
    }

    public void tv_ok_onClick(View view) {
        if (authcode == null) {
            AppUtil.tip(CellnumActivity.this, "您还没有获取验证码或验证码已经失效。");
            return;
        }
        String authcode_new = et_authcode.getText().toString();
        if (!authcode.equals(authcode_new)) {
            AppUtil.tip(CellnumActivity.this, "验证码输入错误。");
            return;
        }
        if (type == AppConst.TYPE_SIGNIN) {
            Intent intent = new Intent(CellnumActivity.this, PasswordActivity.class);
            intent.putExtra("cellnum", cellnum);
            intent.putExtra("type", AppConst.TYPE_SIGNIN);
            startActivity(intent);
        } else {
            if (okThread == null || !okThread.isAlive()) {
                okThread = new Thread(okRunnable);
                okThread.start();
            }
        }
    }

}
