package com.subang.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.subang.api.ActivityAPI;
import com.subang.api.UserAPI;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.bean.Result;
import com.subang.domain.TicketType;
import com.subang.domain.User;
import com.subang.util.WebConst;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MallActivity extends Activity {

    private AppShare appShare;
    private User user;

    private ListView lv_ticketType;

    private SimpleAdapter ticketTypeAdapter;

    private Thread thread, operaThread;
    private List<TicketType> ticketTypes;
    private List<Map<String, Object>> ticketTypeItems;

    private AlertDialog confirmDialog, promptDialog;

    private View.OnClickListener exchangeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TicketType ticketType = (TicketType) v.getTag(R.id.key_data);
            if (operaThread != null && operaThread.isAlive()) {
                return;
            }
            if (user.getScore() < ticketType.getScore()) {
                promptDialog.show();
                return;
            }
            operaThread = new OperaThread(ticketType);
            confirmDialog.show();
        }
    };

    private AlertDialog.OnClickListener dialogOnClickListener = new AlertDialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                operaThread.start();
            }
        }
    };

    private AdapterView.OnItemClickListener ticketTypeOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MallActivity.this, WebActivity.class);
            intent.putExtra("title", "速帮优惠券");
            intent.putExtra("url", WebConst.HOST_URI + "weixin/activity/detail.html?tickettypeid=" + ticketTypes.get(position).getId());
            startActivity(intent);
        }
    };


    private SimpleAdapter.ViewBinder ticketTypeViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.iv_icon && data instanceof Bitmap) {
                ((ImageView) view).setImageBitmap((Bitmap) data);
                return true;
            }
            if (view.getId() == R.id.tv_exchange) {
                view.setTag(R.id.key_data, data);
                view.setOnClickListener(exchangeOnClickListener);
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(MallActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    ticketTypeItems.clear();
                    Map<String, Object> ticketTypeItem;
                    AppUtil.conf(MallActivity.this);
                    for (TicketType ticketType : ticketTypes) {
                        ticketTypeItem = new HashMap<>();
                        Bitmap bitmap = BitmapFactory.decodeFile(AppConf.basePath + ticketType.getIcon());
                        ticketTypeItem.put("icon", bitmap);
                        ticketTypeItem.put("name", ticketType.getName());
                        ticketTypeItem.put("money", "金额：" + ticketType.getMoney());
                        ticketTypeItem.put("score", "积分：" + ticketType.getScore());
                        ticketTypeItem.put("deadline", "截止期：" + ticketType.getDeadlineDes());
                        ticketTypeItem.put("ticketType", ticketType);
                        ticketTypeItems.add(ticketTypeItem);
                    }
                    lv_ticketType.setBackgroundResource(android.R.color.transparent);
                    ticketTypeAdapter.notifyDataSetChanged();
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    AppUtil.tip(MallActivity.this, ComUtil.getInfo(msg));
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(MallActivity.this);
            TicketType filter = new TicketType();
            filter.setId(0);
            filter.setName("");
            filter.setIcon("");
            filter.setMoney(0.0);
            filter.setScore(0);
            filter.setDeadline(new Timestamp(System.currentTimeMillis()));
            ticketTypes = ActivityAPI.listTicketType(filter);
            if (ticketTypes == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        appShare.map.put("mine.refresh", true);
        user = (User) getIntent().getSerializableExtra("user");
        setContentView(R.layout.activity_mall);
        findView();
        ticketTypeItems = new ArrayList<>();
        ticketTypeAdapter = new SimpleAdapter(MallActivity.this, ticketTypeItems, R.layout.item_ticket_type, new
                String[]{"icon", "name", "money", "score", "deadline", "ticketType"},
                new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_money, R.id.tv_score, R.id.tv_deadline, R.id.tv_exchange});
        ticketTypeAdapter.setViewBinder(ticketTypeViewBinder);
        lv_ticketType.setAdapter(ticketTypeAdapter);
        lv_ticketType.setOnItemClickListener(ticketTypeOnItemClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(MallActivity.this)
                .setMessage("确定要兑换此优惠券吗？")
                .setNegativeButton("取消", dialogOnClickListener)
                .setPositiveButton("确定", dialogOnClickListener);
        confirmDialog = builder.create();

        builder = new AlertDialog.Builder(MallActivity.this)
                .setMessage("您的积分不足，无法兑换优惠券。")
                .setNegativeButton("确定", dialogOnClickListener);
        promptDialog = builder.create();

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        lv_ticketType = (ListView) findViewById(R.id.lv_ticket_type);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    private class OperaThread extends Thread {

        private TicketType ticketType;

        public OperaThread(TicketType ticketType) {
            super();
            this.ticketType = ticketType;
        }

        @Override
        public void run() {
            AppUtil.confApi(MallActivity.this);
            Result result = UserAPI.addTicket(ticketType.getId());
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            String info;
            if (result.getCode().equals(Result.OK)) {
                user.setScore(user.getScore() - ticketType.getScore());
                info = "兑换成功。";
            } else {
                info = "兑换失败。" + result.getMsg();
            }
            Message msg = ComUtil.getMessage(AppConst.WHAT_SUCC_SUBMIT, info);
            handler.sendMessage(msg);
        }
    }
}
