package com.subang.app.activity;

import android.app.Activity;
import android.content.ComponentName;
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

import com.subang.api.UserAPI;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.TicketDetail;
import com.subang.util.WebConst;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketActivity extends Activity {

    private ComponentName callingActivity;
    private Integer categoryid;

    private ListView lv_ticket;

    private SimpleAdapter ticketAdapter;

    private Thread thread;
    private List<TicketDetail> ticketDetails;
    private List<Map<String, Object>> ticketItems;


    private AdapterView.OnItemClickListener ticketOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (callingActivity == null) {
                return;
            }
            Intent intent = getIntent();
            intent.putExtra("ticketid", ticketDetails.get(position).getId());
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private SimpleAdapter.ViewBinder ticketViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.iv_icon && data instanceof Bitmap) {
                ((ImageView) view).setImageBitmap((Bitmap) data);
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
                    AppUtil.networkTip(TicketActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    ticketItems.clear();
                    Map<String, Object> ticketItem;
                    AppUtil.conf(TicketActivity.this);
                    for (TicketDetail ticketDetail : ticketDetails) {
                        ticketItem = new HashMap<>();
                        Bitmap bitmap = BitmapFactory.decodeFile(AppConf.basePath + ticketDetail.getIcon());
                        ticketItem.put("icon", bitmap);
                        ticketItem.put("name", ticketDetail.getName());
                        ticketItem.put("money", "金额：" + ticketDetail.getMoney());
                        ticketItem.put("deadline", "截止期：" + ticketDetail.getDeadlineDes());
                        ticketItems.add(ticketItem);
                    }
                    if (ticketDetails.isEmpty()) {
                        lv_ticket.setBackgroundResource(R.drawable.listview_no_ticket);
                    } else {
                        lv_ticket.setBackgroundResource(android.R.color.transparent);
                    }
                    ticketAdapter.notifyDataSetChanged();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(TicketActivity.this);
            TicketDetail filter = new TicketDetail();
            filter.setId(0);
            filter.setName("");
            filter.setIcon("");
            filter.setMoney(0.0);
            filter.setDeadline(new Timestamp(System.currentTimeMillis()));
            ticketDetails = UserAPI.listTicket(categoryid, filter);
            if (ticketDetails == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callingActivity = getCallingActivity();
        if (callingActivity != null) {
            categoryid = getIntent().getIntExtra("categoryid", 0);
        } else {
            categoryid = null;
        }

        setContentView(R.layout.activity_ticket);
        findView();
        ticketItems = new ArrayList<>();
        ticketAdapter = new SimpleAdapter(TicketActivity.this, ticketItems, R.layout.item_ticket, new
                String[]{"icon", "name", "money", "deadline"},
                new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_money, R.id.tv_deadline});
        ticketAdapter.setViewBinder(ticketViewBinder);

        lv_ticket.setAdapter(ticketAdapter);
        lv_ticket.setOnItemClickListener(ticketOnItemClickListener);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        lv_ticket = (ListView) findViewById(R.id.lv_ticket);
    }

    public void tv_intro_onClick(View view) {
        Intent intent = new Intent(TicketActivity.this, WebActivity.class);
        intent.putExtra("title", "优惠券使用说明");
        intent.putExtra("url", WebConst.HOST_URI + "content/weixin/ticket/intro.htm");
        startActivity(intent);
    }

    public void iv_back_onClick(View view) {
        finish();
    }
}
