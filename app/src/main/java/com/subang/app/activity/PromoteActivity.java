package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PromoteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote);
    }
    public void btn_promote_onClick(View view) {
        Intent intent = new Intent(PromoteActivity.this, PromQrcodeActivity.class);
        startActivity(intent);
    }
    public void tv_tip_onClick(View view) {
        Intent intent = new Intent(PromoteActivity.this, PromRuleActivity.class);
        startActivity(intent);
    }
    public void iv_back_onClick(View view) {
        finish();
    }
}
