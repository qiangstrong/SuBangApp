package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.subang.api.InfoAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.Result;

public class FeedbackActivity extends Activity {

    private RatingBar rb_whole, rb_wash, rb_service, rb_app;
    private EditText et_text;

    private Thread thread;
    private String comment;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(FeedbackActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    AppUtil.tip(FeedbackActivity.this, "反馈提交成功，谢谢。");
                    FeedbackActivity.this.finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(FeedbackActivity.this);
            Result result = InfoAPI.addFeedback(comment);
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
        setContentView(R.layout.activity_feedback);
        findView();
    }

    private void findView() {
        rb_whole = (RatingBar) findViewById(R.id.rb_whole);
        rb_wash = (RatingBar) findViewById(R.id.rb_wash);
        rb_service = (RatingBar) findViewById(R.id.rb_service);
        rb_app = (RatingBar) findViewById(R.id.rb_app);
        et_text = (EditText) findViewById(R.id.et_text);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void tv_ok_onClick(View view) {
        prepare();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void prepare() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("整体评价：" + rb_whole.getProgress() + "。");
        buffer.append("洗衣质量：" + rb_wash.getProgress() + "。");
        buffer.append("服务态度：" + rb_service.getProgress() + "。");
        buffer.append("软件质量：" + rb_app.getProgress() + "。");
        buffer.append("备注：" + et_text.getText().toString());
        comment = buffer.toString();
    }
}
