package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.subang.api.OrderAPI;
import com.subang.api.UserAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.AddrDetail;
import com.subang.domain.Category;
import com.subang.domain.Order;
import com.subang.util.TimeUtil;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public class AddOrderActivity extends Activity {

    private static final int REGUEST_CODE_ADDR = 0;
    private static final int REGUEST_CODE_DATE = 1;

    private AppShare appShare;

    private TextView tv_title, tv_addAddr, tv_name, tv_cellnum, tv_detail, tv_date;
    private EditText et_comment;
    private Button btn_add;
    private RelativeLayout rl_addr;

    private Thread thread, submitThread;
    private Category category;
    private AddrDetail addr;
    private TimeUtil.Option dateOption, timeOption;
    private boolean isAddr = false;
    private boolean isDate = false;
    private Order order;

    private boolean isLoaded = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(AddOrderActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    if (addr == null) {
                        tv_addAddr.setVisibility(View.VISIBLE);
                        rl_addr.setVisibility(View.GONE);
                    } else {
                        tv_addAddr.setVisibility(View.GONE);
                        rl_addr.setVisibility(View.VISIBLE);
                        tv_name.setText(addr.getName());
                        tv_cellnum.setText(addr.getCellnum());
                        tv_detail.setText(addr.getDetail());
                    }
                    isLoaded = true;
                    isAddr = true;
                    prepare();
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT:{
                    appShare.map.put("main.position",1);
                    appShare.map.put("order.position",0);
                    appShare.map.put("refresh",true);
                    Intent intent=new Intent(AddOrderActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AddOrderActivity.this);
            AddrDetail filter = new AddrDetail();
            List<AddrDetail> addrDetails = UserAPI.listAddr(filter);
            if (addrDetails == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            if (addrDetails.size() != 0) {
                addr = UserAPI.getDefaultAddr();
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AddOrderActivity.this);
            Map<String, String> errors= OrderAPI.add(order);
            if (errors==null){
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);            //到订单列表界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare=(AppShare)getApplication();
        setContentView(R.layout.activity_add_order);
        findView();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
        category = (Category) getIntent().getSerializableExtra("category");
        order = new Order();
        order.setCategoryid(category.getId());
        tv_title.setText(category.getName());
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REGUEST_CODE_ADDR: {
                break;
            }
            case REGUEST_CODE_DATE: {
                if (resultCode == RESULT_OK) {
                    Bundle bundle = intent.getExtras();
                    dateOption = (TimeUtil.Option) bundle.get("date");
                    timeOption = (TimeUtil.Option) bundle.get("time");
                    tv_date.setText(new StringBuilder().append(" ").append(dateOption.getText()).append(" ").append
                            (timeOption.getText()).toString());
                    isDate = true;
                    prepare();
                }
                break;
            }
        }
    }

    private void findView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_addAddr = (TextView) findViewById(R.id.tv_add_addr);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_cellnum = (TextView) findViewById(R.id.tv_cellnum);
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        tv_date = (TextView) findViewById(R.id.tv_date);
        et_comment = (EditText) findViewById(R.id.et_comment);
        btn_add = (Button) findViewById(R.id.btn_add);
        rl_addr = (RelativeLayout) findViewById(R.id.rl_addr);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void ll_price_onClick(View view) {
        //跳转到价目表的activity
    }

    public void tv_addAddr_onClick(View view) {
        //到添加地址activity
    }

    public void rl_addr_onClick(View view) {
        //到查看地址activity
    }

    public void rl_date_onClick(View view) {
        Intent intent = new Intent(AddOrderActivity.this, DateActivity.class);
        startActivityForResult(intent, REGUEST_CODE_DATE);
    }

    public void btn_add_onClick(View view) {
        order.setAddrid(addr.getId());
        order.setDate((Date) dateOption.getValue());
        order.setTime((Integer) timeOption.getValue());
        order.setUserComment(et_comment.getText().toString());
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
    }

    private void prepare() {
        if (isAddr && isDate) {
            btn_add.setEnabled(true);
        }else {
            btn_add.setEnabled(false);
        }
    }
}
