package com.subang.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.OrderAPI;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.BasePrepayResult;
import com.subang.bean.OrderDetail;
import com.subang.bean.PayArg;
import com.subang.bean.TicketDetail;
import com.subang.bean.WeixinPrepayResult;
import com.subang.domain.Payment;
import com.subang.util.WebConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayActivity extends Activity {

    private AppShare appShare;
    private Integer orderid;

    private TextView tv_orderno, tv_actualMoney, tv_payment, tv_ticket;
    private RelativeLayout rl_ticket;
    private LinearLayout ll_ticket;
    private ImageView iv_icon;
    private TextView tv_name, tv_money, tv_deadline;
    private ListView lv_payType;
    private ProgressDialog progressDialog;

    private Thread thread, submitThread;
    private OrderDetail orderDetail;
    private List<Map<String, Object>> payTypeItems;

    private Integer selectedPayTypeIndex = null;
    private TicketDetail ticketDetail = null;
    private PayArg payArg;
    private BasePrepayResult basePrepayResult;

    private boolean isLoaded = false;

    private AdapterView.OnItemClickListener payTypeOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (selectedPayTypeIndex != null) {
                if (selectedPayTypeIndex == position) {
                    return;
                }
                CheckBox chk_select = (CheckBox) lv_payType.getChildAt(selectedPayTypeIndex).findViewById(R.id.chk_select);
                chk_select.setChecked(false);
            }
            selectedPayTypeIndex = position;
            CheckBox chk_select = (CheckBox) view.findViewById(R.id.chk_select);
            chk_select.setChecked(true);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(PayActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    tv_orderno.setText("订单编号：" + orderDetail.getOrderno());
                    tv_actualMoney.setText("实付金额：" + orderDetail.getActualMoneyDes());
                    tv_payment.setText(orderDetail.getPaymentDes());
                    if (orderDetail.isTicket()){
                        rl_ticket.setVisibility(View.GONE);
                    }
                    isLoaded = true;
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    switch (basePrepayResult.getCodeEnum()) {
                        case succ: {
                            Intent intent = new Intent(PayActivity.this, PayResultActivity.class);
                            intent.putExtra("payresult", basePrepayResult);
                            startActivity(intent);
                            finish();
                            break;
                        }
                        case fail: {
                            Intent intent = new Intent(PayActivity.this, PayResultActivity.class);
                            intent.putExtra("payresult", basePrepayResult);
                            startActivity(intent);
                            break;
                        }
                        case conti: {
                            prepay();
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PayActivity.this);
            orderDetail = OrderAPI.get(WebConst.ORDER_GET_ID, orderid.toString());
            if (orderDetail == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(PayActivity.this);
            basePrepayResult = OrderAPI.prepay(payArg);
            if (basePrepayResult == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        appShare.map.put("type.refresh", true);
        appShare.map.remove("recharge");
        appShare.map.put("pay", true);
        orderid = getIntent().getIntExtra("orderid", 0);
        setContentView(R.layout.activity_pay);
        findView();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
        initView_payType();
        payArg = new PayArg();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            ticketDetail = (TicketDetail) intent.getSerializableExtra("ticketDetail");
            tv_ticket.setVisibility(View.GONE);
            ll_ticket.setVisibility(View.VISIBLE);
            AppUtil.conf(PayActivity.this);
            Bitmap bitmap = BitmapFactory.decodeFile(AppConf.basePath + ticketDetail.getIcon());
            iv_icon.setImageBitmap(bitmap);
            tv_name.setText(ticketDetail.getName());
            tv_money.setText("金额：" + ticketDetail.getMoney());
            tv_deadline.setText("截止期：" + ticketDetail.getDeadlineDes());
        }
    }

    private void findView() {
        tv_orderno = (TextView) findViewById(R.id.tv_orderno);
        tv_actualMoney = (TextView) findViewById(R.id.tv_actual_money);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_ticket = (TextView) findViewById(R.id.tv_ticket);
        rl_ticket = (RelativeLayout) findViewById(R.id.rl_ticket);
        ll_ticket = (LinearLayout) findViewById(R.id.ll_ticket);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_money = (TextView) findViewById(R.id.tv_money);
        tv_deadline = (TextView) findViewById(R.id.tv_deadline);
        lv_payType = (ListView) findViewById(R.id.lv_pay_type);
    }

    private void initView_payType() {
        createPayTypeItems();
        SimpleAdapter payTypeAdapter = new SimpleAdapter(PayActivity.this, payTypeItems, R.layout.item_pay_type,
                new String[]{"icon", "name"}, new int[]{R.id.iv_icon, R.id.tv_name});
        lv_payType.setAdapter(payTypeAdapter);
        lv_payType.setOnItemClickListener(payTypeOnItemClickListener);
        lv_payType.post(new Runnable() {
            @Override
            public void run() {
                lv_payType.performItemClick(lv_payType.getChildAt(0), 0, lv_payType.getAdapter().getItemId(0));
            }
        });
    }

    private void createPayTypeItems() {
        payTypeItems = new ArrayList<>();
        Map<String, Object> payTypeItem;

        payTypeItem = new HashMap<>();
        payTypeItem.put("icon", R.drawable.balance_pay_icon);
        payTypeItem.put("name", "余额支付");
        payTypeItem.put("type", Payment.PayType.balance);
        payTypeItems.add(payTypeItem);

        payTypeItem = new HashMap<>();
        payTypeItem.put("icon", R.drawable.wexin_pay_icon);
        payTypeItem.put("name", "微信支付");
        payTypeItem.put("type", Payment.PayType.weixin);
        payTypeItems.add(payTypeItem);

//        payTypeItem = new HashMap<>();
//        payTypeItem.put("icon", R.drawable.ali_pay_icon);
//        payTypeItem.put("name", "支付宝支付");
//        payTypeItem.put("type", Payment.PayType.alipay);
//        payTypeItems.add(payTypeItem);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void rl_ticket_onClick(View view) {
        if (!isLoaded){
            return;
        }
        Intent intent = new Intent(PayActivity.this, TicketActivity.class);
        intent.putExtra("categoryid", orderDetail.getCategoryid());
        startActivityForResult(intent, 0);
    }

    public void btn_pay_onClick(View view) {
        payArg.setClient(PayArg.Client.user);
        payArg.setPayType((Payment.PayType) payTypeItems.get(selectedPayTypeIndex).get("type"));
        payArg.setOrderid(orderid);
        if (ticketDetail != null) {
            payArg.setTicketid(ticketDetail.getId());
        }
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
        progressDialog = ProgressDialog.show(this, "提示", "正在获取预支付订单...");
    }

    private void prepay() {
        switch (payArg.getPayTypeEnum()) {
            case weixin: {
                WeixinPrepayResult weixinPrepayResult = (WeixinPrepayResult) basePrepayResult;
                Intent intent = new Intent(PayActivity.this, PrepayActivity.class);
                intent.putExtra("payrequest", weixinPrepayResult.getArg());
                startActivity(intent);
                break;
            }
            case alipay: {

            }
        }
    }
}
