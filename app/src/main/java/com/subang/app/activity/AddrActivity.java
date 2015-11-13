package com.subang.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.subang.api.UserAPI;
import com.subang.app.helper.AddrDataHelper;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.applib.util.ComUtil;
import com.subang.applib.util.SwipeMenu;
import com.subang.applib.util.SwipeMenuCreator;
import com.subang.applib.util.SwipeMenuItem;
import com.subang.applib.view.SwipeMenuListView;
import com.subang.bean.AddrDetail;
import com.subang.bean.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddrActivity extends Activity {

    private AppShare appShare;
    private ComponentName callingActivity;

    private SwipeMenuListView lv_addr;
    private SimpleAdapter addrSimpleAdapter;

    private Thread thread, operaThread;
    private List<AddrDetail> addrs;
    private List<Map<String, Object>> addrItems;

    private boolean isLoaded = false;

    private AdapterView.OnItemClickListener addrOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (callingActivity == null) {
                return;
            }
            //如果数据正在更新，不响应用户点击
            if ((thread != null && thread.isAlive()) || (operaThread != null && operaThread.isAlive())) {
                return;
            }
            Intent intent = getIntent();
            intent.putExtra("addr", addrs.get(position));
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
            deleteItem.setWidth(ComUtil.dp2px(AddrActivity.this, 90));
            deleteItem.setIcon(R.drawable.delete_icon);
            menu.addMenuItem(deleteItem);
        }
    };

    private SwipeMenuListView.OnMenuItemClickListener addrOnMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            if (operaThread == null || !operaThread.isAlive()) {
                operaThread = new OperaThread(addrs.get(position).getId());
                operaThread.start();
            }
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(AddrActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    addrItems.clear();
                    Map<String, Object> addrItem;
                    for (AddrDetail addr : addrs) {
                        addrItem = new HashMap<String, Object>();
                        addrItem.put("name", addr.getName());
                        addrItem.put("cellnum", addr.getCellnum());
                        addrItem.put("area", AddrDataHelper.getAreaDes(addr));
                        addrItem.put("detail", addr.getDetail());
                        addrItems.add(addrItem);
                    }
                    if (addrs.isEmpty()) {
                        lv_addr.setBackgroundResource(R.drawable.listview_no_addr);
                    } else {
                        lv_addr.setBackgroundResource(android.R.color.transparent);
                    }
                    addrSimpleAdapter.notifyDataSetChanged();
                    isLoaded = true;
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    Bundle bundle = msg.getData();
                    Integer addrid = (Integer) bundle.get("addrid");
                    String info = bundle.getString("info");
                    if (thread == null || !thread.isAlive()) {
                        thread = new Thread(runnable);
                        thread.start();
                    }
                    Toast toast = Toast.makeText(AddrActivity.this, info, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(AddrActivity.this);
            addrs = UserAPI.listAddr(null);
            if (addrs == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);           //加载数据失败
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);                //加载数据成功
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getApplication();
        callingActivity = getCallingActivity();
        setContentView(R.layout.activity_addr);
        findView();
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        addrItems = new ArrayList<>();
        addrSimpleAdapter = new SimpleAdapter(AddrActivity.this, addrItems, R.layout.item_addr,
                new String[]{"name", "cellnum", "area", "detail"},
                new int[]{R.id.tv_name, R.id.tv_cellnum, R.id.tv_area, R.id.tv_detail});
        lv_addr.setAdapter(addrSimpleAdapter);
        lv_addr.setOnItemClickListener(addrOnItemClickListener);
        lv_addr.setMenuCreator(swipeMenuCreator);
        lv_addr.setOnMenuItemClickListener(addrOnMenuItemClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refresh;
        if (appShare.map.containsKey("addr.refresh")) {
            refresh = (boolean) appShare.map.get("addr.refresh");
            appShare.map.remove("addr.refresh");
            if (refresh) {
                if (thread == null || !thread.isAlive()) {
                    thread = new Thread(runnable);
                    thread.start();
                }
            }
        }
    }

    private void findView() {
        lv_addr = (SwipeMenuListView) findViewById(R.id.lv_addr);
    }

    public void iv_back_onClick(View view) {
        finish();
    }

    public void tv_add_onClick(View view) {
        Intent intent = new Intent(AddrActivity.this, AddAddrActivity.class);
        startActivity(intent);
    }

    private class OperaThread extends Thread {

        private Integer addrid;

        public OperaThread(Integer addrid) {
            super();
            this.addrid = addrid;
        }

        @Override
        public void run() {
            AppUtil.confApi(AddrActivity.this);
            Result result = UserAPI.deleteAddr(addrid);
            if (result == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            Message msg = new Message();
            msg.what = AppConst.WHAT_SUCC_SUBMIT;
            Bundle bundle = new Bundle();
            bundle.putInt("addrid", addrid);
            bundle.putString("info", "地址删除成功。");
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

}
