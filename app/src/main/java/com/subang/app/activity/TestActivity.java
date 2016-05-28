package com.subang.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.subang.app.util.AppConst;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestActivity extends Activity {

    private SmsReceiver smsReceiver;

    private TextView tv_msg,tv_code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findView();
        registerSmsReceiver();
    }

    private void findView(){
        tv_msg=(TextView)findViewById(R.id.tv_msg);
        tv_code=(TextView)findViewById(R.id.tv_code);
    }

    public void onClick(View view) {

    }


    private void registerSmsReceiver() {
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        registerReceiver(smsReceiver, filter);
    }

    private class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(AppConst.LOG_TAG,"SmsReceiver");
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[])pdu);
                String message = sms.getMessageBody();
                tv_msg.setText(message);
                parseCode(message);
            }
        }
    }

    private void parseCode(String message) {
        String regex = "^【速帮家庭服务平台】.*(\\d{4}).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            tv_code.setText(matcher.group(1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
    }

}
