package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.OrderAPI;
import com.subang.app.helper.AddrDataHelper;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.bean.OrderDetail;
import com.subang.domain.Clothes;
import com.subang.domain.History;
import com.subang.util.WebConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends Activity {

    private static final int WHAT_DETAIL = 1;
    private static final int WHAT_CLOTHES = 2;
    private static final int WHAT_HISTORY = 3;

    private TextView tv_orderno, tv_datetime, tv_categoryname, tv_userComment, tv_addrname, tv_addrcellnum,
            tv_area, tv_addrdetail, tv_workername, tv_workercellnum, tv_workerComment, tv_payType, tv_payment,
            tv_actualMoney, tv_clothesNum;
    private ListView lv_clothes, lv_history;
    private RelativeLayout rl_pay, rl_clothes;

    private Thread thread;
    private Integer orderid;
    private OrderDetail orderDetail;
    private List<Clothes> clothess;
    private List<History> historys;
    private List<Map<String, Object>> clothesItems;
    private List<Map<String, Object>> historyItems;

    private SimpleAdapter clothesSimpleAdapter, historySimpleAdapter;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(OrderDetailActivity.this);
                    break;
                }
                case WHAT_DETAIL: {
                    showDetail();
                    break;
                }
                case WHAT_CLOTHES: {
                    showClothes();
                    break;
                }
                case WHAT_HISTORY: {
                    showHistory();
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(OrderDetailActivity.this);
            orderDetail = OrderAPI.get(WebConst.ORDER_GET_ID, orderid.toString());
            if (orderDetail == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_DETAIL);
            if (orderDetail.isChecked()) {
                clothess = OrderAPI.listClothes(orderid, null);
                if (clothess == null) {
                    handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                    return;
                }
                handler.sendEmptyMessage(WHAT_CLOTHES);
            }
            historys = OrderAPI.listHistory(orderid, null);
            if (historys == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_HISTORY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        findView();
        orderid = getIntent().getIntExtra("orderid", 0);

        clothesItems = new ArrayList<>();
        historyItems = new ArrayList<>();
        clothesSimpleAdapter = new SimpleAdapter(OrderDetailActivity.this, clothesItems, R.layout.item_clothes,
                new String[]{"name", "color", "flaw"}, new int[]{R.id.tv_name, R.id.tv_color, R.id.tv_flaw});
        historySimpleAdapter = new SimpleAdapter(OrderDetailActivity.this, historyItems, R.layout.item_history,
                new String[]{"operation", "time"}, new int[]{R.id.tv_operation, R.id.tv_time});
        lv_clothes.setAdapter(clothesSimpleAdapter);
        lv_history.setAdapter(historySimpleAdapter);

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        tv_orderno = (TextView) findViewById(R.id.tv_orderno);
        tv_datetime = (TextView) findViewById(R.id.tv_datetime);
        tv_categoryname = (TextView) findViewById(R.id.tv_categoryname);
        tv_userComment = (TextView) findViewById(R.id.tv_user_comment);
        tv_addrcellnum = (TextView) findViewById(R.id.tv_addrcellnum);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_addrdetail = (TextView) findViewById(R.id.tv_addrdetail);
        tv_workercellnum = (TextView) findViewById(R.id.tv_workercellnum);
        tv_workerComment = (TextView) findViewById(R.id.tv_worker_comment);
        tv_payType = (TextView) findViewById(R.id.tv_pay_type);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_clothesNum = (TextView) findViewById(R.id.tv_clothes_num);
        tv_addrname = (TextView) findViewById(R.id.tv_addrname);
        tv_workername = (TextView) findViewById(R.id.tv_workername);
        tv_actualMoney = (TextView) findViewById(R.id.tv_actual_money);

        lv_clothes = (ListView) findViewById(R.id.lv_clothes);
        lv_history = (ListView) findViewById(R.id.lv_history);

        rl_pay = (RelativeLayout) findViewById(R.id.rl_pay);
        rl_clothes = (RelativeLayout) findViewById(R.id.rl_clothes);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    private void showDetail() {
        //订单信息
        tv_orderno.setText(orderDetail.getOrderno());
        tv_datetime.setText(orderDetail.getDateDes() + " " + orderDetail.getTimeDes());
        tv_categoryname.setText(orderDetail.getCategoryname());
        tv_userComment.setText(orderDetail.getUserComment());

        //地址信息
        tv_addrname.setText(orderDetail.getAddrname());
        tv_addrcellnum.setText(orderDetail.getAddrcellnum());
        tv_area.setText(AddrDataHelper.getAreaDes(orderDetail));
        tv_addrdetail.setText(orderDetail.getAddrdetail());

        //取衣员信息
        tv_workername.setText(orderDetail.getWorkername());
        tv_workercellnum.setText(orderDetail.getWorkercellnum());
        tv_workerComment.setText(orderDetail.getWorkerComment());

        //支付信息
        if (orderDetail.isPaid()) {
            rl_pay.setVisibility(View.VISIBLE);
            tv_payType.setText(orderDetail.getPayTypeDes());
            tv_payment.setText(orderDetail.getPaymentDes());
            tv_actualMoney.setText(orderDetail.getActualMoneyDes());
        }
    }

    private void showClothes() {
        rl_clothes.setVisibility(View.VISIBLE);
        tv_clothesNum.setText(clothess.size() + "件");
        clothesItems.clear();
        Map<String, Object> clothesItem;
        for (Clothes clothes : clothess) {
            clothesItem = new HashMap<>();
            clothesItem.put("name", clothes.getName());
            clothesItem.put("color", clothes.getColor());
            clothesItem.put("flaw", clothes.getFlaw());
            clothesItems.add(clothesItem);
        }
        clothesSimpleAdapter.notifyDataSetChanged();
    }

    private void showHistory() {
        historyItems.clear();
        Map<String, Object> historyItem;
        for (History history : historys) {
            historyItem = new HashMap<>();
            historyItem.put("operation", history.getOperationDes());
            historyItem.put("time", history.getTimeDes());
            historyItems.add(historyItem);
        }
        historySimpleAdapter.notifyDataSetChanged();
    }
}
