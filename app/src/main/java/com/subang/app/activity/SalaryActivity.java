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

public class SalaryActivity extends Activity {

    private static final int WHAT_SALARY = 1;
    private static final int WHAT_USER = 2;

    private User user;

    private TextView tv_money;
    private ListView lv_balance, lv_user;

    private SimpleAdapter balanceAdapter, userAdapter;

    private Thread thread;
    private List<Balance> balances;
    private List<Map<String, Object>> balanceItems;
    private List<User> users;
    private List<Map<String, Object>> userItems;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(SalaryActivity.this);
                    break;
                }
                case WHAT_SALARY: {
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
                case WHAT_USER: {
                    userItems.clear();
                    Map<String, Object> userItem;
                    int i=0;
                    for (User user : users) {
                        userItem = new HashMap<>();
                        userItem.put("count",++i);
                        userItem.put("cellnum",user.getHiddenCellnum());
                        userItems.add(userItem);
                    }
                    userAdapter.notifyDataSetChanged();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(SalaryActivity.this);
            balances = UserAPI.listSalary(null);
            if (balances == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_SALARY);
            
            User filter=new User();
            filter.setCellnum("00000000000");
            users=UserAPI.listUser(filter);
            if (users == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_USER);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);
        findView();
        balanceItems = new ArrayList<>();
        balanceAdapter = new SimpleAdapter(SalaryActivity.this, balanceItems, R.layout.item_balance, new
                String[]{"payType", "money", "time"},
                new int[]{R.id.tv_pay_type, R.id.tv_money, R.id.tv_time});
        lv_balance.setAdapter(balanceAdapter);

        userItems = new ArrayList<>();
        userAdapter = new SimpleAdapter(SalaryActivity.this, userItems, R.layout.item_user, new
                String[]{"count", "cellnum"},
                new int[]{R.id.tv_count, R.id.tv_cellnum});
        lv_user.setAdapter(userAdapter);

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        user = (User) getIntent().getSerializableExtra("user");
        tv_money.append(user.getSalary() + "元");
    }

    private void findView() {
        tv_money = (TextView) findViewById(R.id.tv_money);
        lv_balance = (ListView) findViewById(R.id.lv_balance);
        lv_user = (ListView) findViewById(R.id.lv_user);
    }

    public void tv_draw_onClick(View view) {
        Intent intent = new Intent(SalaryActivity.this, DrawActivity.class);
        startActivity(intent);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

}
