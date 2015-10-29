package com.subang.app.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends Activity {

    private TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findView();
        writeUser();
    }

    public void writeUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.file_user), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cellnum", "15502457990");
        editor.putString("password", "123");
        editor.commit();
    }

    private void findView() {
        tv_content = (TextView) findViewById(R.id.tv_content);
    }
}
