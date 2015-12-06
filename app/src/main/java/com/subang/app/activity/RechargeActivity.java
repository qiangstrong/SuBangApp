package com.subang.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.ActivityAPI;
import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.BasePrepayResult;
import com.subang.bean.PayArg;
import com.subang.bean.WeixinPrepayResult;
import com.subang.domain.Payment;
import com.subang.domain.Rebate;
import com.subang.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RechargeActivity extends Activity {

    private AppShare appShare;
    private User user;

    private TextView tv_cellnum, tv_money;
    private EditText et_text;
    private GridView gv_rebate;
    private ListView lv_payType;
    private Button btn_recharge;
    private ProgressDialog progressDialog;

    private Thread thread, submitThread;
    private List<Rebate> rebates;
    private List<Map<String, Object>> rebateItems, payTypeItems;
    private SimpleAdapter rebateAdapter;

    private Integer selectedRebateIndex = null;
    private Integer selectedPayTypeIndex = null;
    private PayArg payArg;
    private BasePrepayResult basePrepayResult;

    private AdapterView.OnItemClickListener rebateOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            select_rebate(position);
        }
    };

    private View.OnFocusChangeListener rebateOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            select_rebate(rebates.size());
        }
    };

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
                    AppUtil.networkTip(RechargeActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    initView_rebate();
                    btn_recharge.setEnabled(true);
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    switch (basePrepayResult.getCodeEnum()) {
                        case succ: {
                            Intent intent = new Intent(RechargeActivity.this, PayResultActivity.class);
                            intent.putExtra("payresult", basePrepayResult);
                            startActivity(intent);
                            finish();
                            break;
                        }
                        case fail: {
                            Intent intent = new Intent(RechargeActivity.this, PayResultActivity.class);
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
            AppUtil.confApi(RechargeActivity.this);
            rebates = ActivityAPI.listRebate(null);
            if (rebates == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(RechargeActivity.this);
            basePrepayResult = UserAPI.prepay(payArg);
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
        appShare.map.put("mine.refresh", true);
        appShare.map.put("recharge", true);     //指示支付成功后，在WXPayEntryActivity中如何跳转
        user = (User) getIntent().getSerializableExtra("user");
        setContentView(R.layout.activity_recharge);
        findView();
        tv_cellnum.setText(" " + user.getCellnum());
        tv_money.setText(" 余额：" + user.getMoney().toString());

        rebateItems = new ArrayList<>();
        rebateAdapter = new SimpleAdapter(RechargeActivity.this, rebateItems, R.layout.item_rebate,
                new String[]{"text"}, new int[]{R.id.tv_text});
        gv_rebate.setAdapter(rebateAdapter);
        gv_rebate.setOnItemClickListener(rebateOnItemClickListener);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
        initView_payType();
        payArg = new PayArg();
    }

    private void findView() {
        tv_cellnum = (TextView) findViewById(R.id.tv_cellnum);
        tv_money = (TextView) findViewById(R.id.tv_money);
        gv_rebate = (GridView) findViewById(R.id.gv_rebate);
        lv_payType = (ListView) findViewById(R.id.lv_pay_type);
        btn_recharge = (Button) findViewById(R.id.btn_recharge);
    }

    private void initView_rebate() {
        rebateItems.clear();
        Map<String, Object> rebateItem;
        for (Rebate rebate : rebates) {
            rebateItem = new HashMap<>();
            rebateItem.put("text", rebate.toString());
            rebateItems.add(rebateItem);
        }
        rebateItem = new HashMap<>();
        rebateItem.put("text", "");
        rebateItems.add(rebateItem);

        rebateAdapter.notifyDataSetChanged();
        gv_rebate.post(new Runnable() {
            @Override
            public void run() {
                View view = gv_rebate.getChildAt(rebates.size());
                view.findViewById(R.id.tv_text).setVisibility(View.INVISIBLE);
                et_text = (EditText) view.findViewById(R.id.et_text);
                et_text.setVisibility(View.VISIBLE);
                et_text.setOnFocusChangeListener(rebateOnFocusChangeListener);

                gv_rebate.performItemClick(gv_rebate.getChildAt(0), 0, gv_rebate.getAdapter().getItemId(0));
            }
        });

    }

    private void initView_payType() {
        createPayTypeItems();
        SimpleAdapter payTypeAdapter = new SimpleAdapter(RechargeActivity.this, payTypeItems, R.layout.item_pay_type,
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

    private void select_rebate(int index) {
        int lastIndex = rebates.size();
        if (selectedRebateIndex != null) {
            if (selectedRebateIndex == index) {
                return;
            }
            View view = gv_rebate.getChildAt(selectedRebateIndex);
            if (selectedRebateIndex != lastIndex) {
                view.findViewById(R.id.iv_select).setVisibility(View.INVISIBLE);
            } else {
                et_text.setText("");
                et_text.clearFocus();
            }

        }
        selectedRebateIndex = index;
        View view = gv_rebate.getChildAt(selectedRebateIndex);
        if (selectedRebateIndex != lastIndex) {
            view.findViewById(R.id.iv_select).setVisibility(View.VISIBLE);
        }
    }

    private void createPayTypeItems() {
        payTypeItems = new ArrayList<>();
        Map<String, Object> payTypeItem;

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

    public void btn_recharge_onClick(View view) {
        et_text.clearFocus();
        payArg.setClient(PayArg.Client.user);
        String msg = getPayMoney();
        if (msg != null) {
            AppUtil.tip(RechargeActivity.this, msg);
            return;
        }
        payArg.setPayType((Payment.PayType) payTypeItems.get(selectedPayTypeIndex).get("type"));
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
        progressDialog = ProgressDialog.show(this, "提示", "正在获取预支付订单...");
    }

    private String getPayMoney() {
        int lastIndex = rebates.size();
        if (selectedRebateIndex != lastIndex) {
            payArg.setMoney(rebates.get(selectedRebateIndex).getMoney());
            return null;
        } else {
            String moneyText = et_text.getText().toString();
            if (moneyText.length() == 0) {
                return "请选择充值金额";
            }
            Double money = null;
            try {
                money = Double.parseDouble(moneyText);
            } catch (Exception e) {
                return "金额输入错误";
            }
            money = com.subang.util.ComUtil.round(money);
            if (!(money > 0 && money < 10000)) {
                return "充值金额必须在0.1元到10000元之间";
            }
            payArg.setMoney(money);
            return null;
        }
    }

    private void prepay() {
        switch (payArg.getPayTypeEnum()) {
            case weixin: {
                WeixinPrepayResult weixinPrepayResult = (WeixinPrepayResult) basePrepayResult;
                Intent intent = new Intent(RechargeActivity.this, PrepayActivity.class);
                intent.putExtra("payrequest", weixinPrepayResult.getArg());
                startActivity(intent);
                break;
            }
            case alipay: {

            }
        }
    }
}
