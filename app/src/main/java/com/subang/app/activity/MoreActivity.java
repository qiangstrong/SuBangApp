package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoreActivity extends Activity {

    private static final int NUM_ACTION = 4;
    private static final int NO_LINE = 0;
    private static final int YES_LINE = 1;

    private ListView lv_action;

    private List<Map<String, Object>> actionItems;

    private AdapterView.OnItemClickListener actionOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: {
                    Intent intent = new Intent(MoreActivity.this, CellnumActivity.class);
                    intent.putExtra("type", AppConst.TYPE_CHANGE);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    Intent intent = new Intent(MoreActivity.this, PasswordActivity.class);
                    intent.putExtra("type", AppConst.TYPE_CHANGE);
                    startActivity(intent);
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    AppUtil.deleteConf(MoreActivity.this);
                    Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private SimpleAdapter.ViewBinder actionViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.v_line) {
                int line = (int) data;
                if (line == YES_LINE) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        findView();
        createItems();
        SimpleAdapter actionSimpleAdapter = new SimpleAdapter(MoreActivity.this, actionItems, R.layout.item_action,
                new String[]{"icon", "text", "line"}, new int[]{R.id.iv_icon, R.id.tv_text, R.id.v_line});
        actionSimpleAdapter.setViewBinder(actionViewBinder);
        lv_action.setAdapter(actionSimpleAdapter);
        lv_action.setOnItemClickListener(actionOnItemClickListener);

    }

    private void findView() {
        lv_action = (ListView) findViewById(R.id.lv_action);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    private void createItems() {
        actionItems = new ArrayList<Map<String, Object>>(NUM_ACTION);
        int[] icons = {R.drawable.more_cellnum, R.drawable.more_password, R.drawable.more_agreement, R.drawable.more_logout};
        String[] texts = {"修改手机号", "修改密码", "用户协议", "退出账号"};
        Map<String, Object> actionItem;
        for (int i = 0; i < NUM_ACTION; i++) {
            actionItem = new HashMap<String, Object>();
            actionItem.put("icon", icons[i]);
            actionItem.put("text", texts[i]);
            actionItem.put("line", NO_LINE);
            actionItems.add(actionItem);
        }
        actionItems.get(1).put("line", YES_LINE);
        actionItems.get(3).put("line", YES_LINE);
    }
}
