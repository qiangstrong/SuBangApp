package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.domain.Balance;
import com.subang.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceActivity extends Activity {

    private User user;

    private TextView tv_money;
    private ListView lv_balance;

    private SimpleAdapter balanceAdapter;

    private Thread thread;
    private List<Balance> balances;
    private List<Map<String, Object>> balanceItems;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(BalanceActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    balanceItems.clear();
                    Map<String, Object> balanceItem;
                    for (Balance balance : balances) {
                        balanceItem = new HashMap<>();
                        balanceItem.put("payType", balance.getPayTypeDes());
                        balanceItem.put("money", balance.getMoneyDes());
                        balanceItem.put("time", balance.getTimeDes());
                        balanceItems.add(balanceItem);
                    }
                    balanceAdapter.notifyDataSetChanged();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(BalanceActivity.this);
            balances = UserAPI.listBalance(null);
            if (balances == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        findView();
        balanceItems = new ArrayList<>();
        balanceAdapter = new SimpleAdapter(BalanceActivity.this, balanceItems, R.layout.item_balance, new
                String[]{"payType", "money", "time"},
                new int[]{R.id.tv_pay_type, R.id.tv_money, R.id.tv_time});
        lv_balance.setAdapter(balanceAdapter);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        user = (User) getIntent().getSerializableExtra("user");
        tv_money.append(user.getMoney() + "元");
    }

    private void findView() {
        tv_money = (TextView) findViewById(R.id.tv_money);
        lv_balance = (ListView) findViewById(R.id.lv_balance);
    }

    public void tv_recharge_onClick(View view) {
        Intent intent = new Intent(BalanceActivity.this, RechargeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    public void iv_back_onClick(View view) {
        finish();
    }


}
