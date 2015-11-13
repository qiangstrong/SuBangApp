package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.subang.api.UserAPI;
import com.subang.app.helper.AddrDataHelper;
import com.subang.app.helper.MyTextWatcher;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.bean.AddrData;
import com.subang.domain.Addr;

import java.util.Map;

public class AddAddrActivity extends Activity {

    private AppShare appShare;

    private TextView tv_area;
    private EditText et_detailAuto, et_detailManu, et_name, et_cellnum;
    private Button btn_add;

    private Thread thread, submitThread;
    private AddrData addrData;              //没有使用AddrData中的list字段
    private boolean isArea = false;
    private Addr addr;

    private boolean isLoaded = false;

    private MyTextWatcher detailAutoWatcher, detailManuWatcher, nameWatcher, cellnumWatcher;

    private MyTextWatcher.OnPrepareListener onPrepareListener = new MyTextWatcher.OnPrepareListener() {
        @Override
        public void onPrepare() {
            if (isArea&&(detailAutoWatcher.isAvail() || detailManuWatcher.isAvail()) &&
                    nameWatcher.isAvail() && cellnumWatcher.isAvail()) {
                btn_add.setEnabled(true);
            } else {
                btn_add.setEnabled(false);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(AddAddrActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    tv_area.setText(AddrDataHelper.getAreaDes(addrData));
                    if (addrData.getDetail() != null) {
                        et_detailAuto.setText(addrData.getDetail());
                    }
                    isArea=true;
                    isLoaded = true;
                    onPrepareListener.onPrepare();
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    appShare.map.put("addr.refresh", true);
                    finish();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AddAddrActivity.this);
            addrData = UserAPI.getAddrData();
            if (addrData == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AddAddrActivity.this);
            Map<String, String> errors = UserAPI.addAddr(addr);
            if (errors == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_SUBMIT);            //到订单列表界面
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        setContentView(R.layout.activity_add_addr);
        findView();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
        addr = new Addr();

        detailAutoWatcher = new MyTextWatcher(1, onPrepareListener);
        detailManuWatcher = new MyTextWatcher(1, onPrepareListener);
        nameWatcher = new MyTextWatcher(1, onPrepareListener);
        cellnumWatcher = new MyTextWatcher(1, onPrepareListener);

        et_detailAuto.addTextChangedListener(detailAutoWatcher);
        et_detailManu.addTextChangedListener(detailManuWatcher);
        et_name.addTextChangedListener(nameWatcher);
        et_cellnum.addTextChangedListener(cellnumWatcher);

        AppUtil.conf(AddAddrActivity.this);
        et_cellnum.setText(AppConf.cellnum);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = intent.getExtras();
            AddrData addrData = (AddrData) bundle.get("addrData");
            AddrDataHelper.copy(addrData, this.addrData);
            tv_area.setText(AddrDataHelper.getAreaDes(this.addrData));
            isArea=true;
            onPrepareListener.onPrepare();
        }
    }

    private void findView() {
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_area = (TextView) findViewById(R.id.tv_area);
        et_detailAuto = (EditText) findViewById(R.id.et_detail_auto);
        et_detailManu = (EditText) findViewById(R.id.et_detail_manu);
        et_name = (EditText) findViewById(R.id.et_name);
        et_cellnum = (EditText) findViewById(R.id.et_cellnum);
        btn_add = (Button) findViewById(R.id.btn_add);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void rl_area_onClick(View view) {
        Intent intent = new Intent(AddAddrActivity.this, AreaActivity.class);
        startActivityForResult(intent, 0);
    }

    public void btn_add_onClick(View view) {
        addr.setName(et_name.getText().toString());
        addr.setCellnum(et_cellnum.getText().toString());
        addr.setDetail(et_detailAuto.getText().toString() + et_detailManu.getText().toString());
        addr.setRegionid(addrData.getDefaultRegionid());
        if (submitThread == null || !submitThread.isAlive()) {
            submitThread = new Thread(submitRunnable);
            submitThread.start();
        }
    }
}
