package com.subang.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.subang.api.OrderAPI;
import com.subang.app.activity.R;
import com.subang.app.adapter.OrderAdapter;
import com.subang.app.fragment.face.OnFrontListener;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.applib.view.XListView;
import com.subang.bean.OrderDetail;
import com.subang.domain.Order;

import java.sql.Date;
import java.util.List;


public class TypeFragment extends Fragment implements OnFrontListener {

    private AppShare appShare;

    private int type;
    private XListView xlv_order;
    private OrderAdapter orderAdapter;

    private Thread thread;
    private OrderAdapter.DataHolder dataHolder;
    private OrderDetail filter;

    private boolean isLoaded = false;

    AdapterView.OnItemClickListener orderOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    XListView.IXListViewListener orderListViewListener = new XListView.IXListViewListener() {
        @Override
        public void onRefresh() {
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(runnable);
                thread.start();
            }
        }

        @Override
        public void onLoadMore() {
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    xlv_order.stopRefresh();
                    AppUtil.networkTip(getActivity());
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    xlv_order.stopRefresh();
                    orderAdapter.notifyDataSetChanged();
                    if (dataHolder.orderDetails.isEmpty()) {
                        xlv_order.setBackgroundResource(R.drawable.order_listview_bg);
                    } else {
                        xlv_order.setBackgroundResource(android.R.color.transparent);
                    }
                    isLoaded = true;
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            List<OrderDetail> orderDetails = OrderAPI.userList(type, filter);
            if (orderDetails == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);           //加载数据失败
                return;
            }
            dataHolder.orderDetails = orderDetails;
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);                //加载数据成功
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare=(AppShare)getActivity().getApplication();
        if (getArguments().containsKey("type")) {
            type = getArguments().getInt("type");
        }
        dataHolder = new OrderAdapter.DataHolder();
        filter = new OrderDetail();
        filter.setId(0);
        filter.setOrderno("");
        filter.setState(Order.State.accepted);
        filter.setDate(new Date(System.currentTimeMillis()));
        filter.setTime(0);
        filter.setMoney(0.0);
        filter.setFreight(0.0);
        filter.setCategoryname("");

        orderAdapter = new OrderAdapter(getActivity(), dataHolder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type, container, false);
        findView(view);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        xlv_order.setAdapter(orderAdapter);
        xlv_order.setOnItemClickListener(orderOnItemClickListener);
        xlv_order.setXListViewListener(orderListViewListener);
        xlv_order.setPullLoadEnable(false);
        xlv_order.setPullRefreshEnable(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refresh;
        if (appShare.map.containsKey("refresh")){
            refresh=(boolean)appShare.map.get("refresh");
            appShare.map.remove("refresh");
            if (refresh){
                if (thread == null || !thread.isAlive()) {
                    thread = new Thread(runnable);
                    thread.start();
                }
            }
        }
    }

    @Override
    public void onFront() {
    }

    private void findView(View view) {
        xlv_order = (XListView) view.findViewById(R.id.xlv_order);
    }
}