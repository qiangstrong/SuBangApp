package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.subang.bean.BasePrepayResult;

public class PayResultActivity extends Activity {

    private TextView tv_code, tv_msg;

    private BasePrepayResult prepayResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        findView();
        prepayResult = (BasePrepayResult) getIntent().getSerializableExtra("payresult");
        switch (prepayResult.getCodeEnum()) {
            case succ: {
                tv_code.setText("支付成功");
                break;
            }
            case fail: {
                tv_code.setText("支付失败");
                tv_msg.setText(prepayResult.getMsg());
                break;
            }
        }
    }

    private void findView() {
        tv_code = (TextView) findViewById(R.id.tv_code);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
    }

    public void iv_back_onClick(View view) {
        finish();
    }
}
