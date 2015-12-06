package com.subang.app.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.subang.api.OrderAPI;
import com.subang.app.activity.OrderDetailActivity;
import com.subang.app.activity.PayActivity;
import com.subang.app.activity.R;
import com.subang.app.activity.RemarkActivity;
import com.subang.app.fragment.face.OnFrontListener;
import com.subang.app.helper.OrderAdapter;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.applib.view.XListView;
import com.subang.bean.OrderDetail;
import com.subang.bean.Result;
import com.subang.domain.Order;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;


public class TypeFragment extends Fragment implements OnFrontListener {

    private AppShare appShare;

    private int type;
    private XListView xlv_order;
    private OrderAdapter orderAdapter;

    private Thread thread, operaThread;
    List<OrderDetail> orderDetails;
    private OrderAdapter.DataHolder dataHolder;
    private OrderDetail filter;

    private boolean isLoaded = false;

    AdapterView.OnItemClickListener orderOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position = position - 1;
            if (position >= 0 && position < orderDetails.size()) {
                Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                intent.putExtra("orderid", orderDetails.get(position).getId());
                startActivity(intent);
            }
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
                    dataHolder.orderDetails = orderDetails;
                    orderAdapter.notifyDataSetChanged();
                    if (orderDetails.isEmpty()) {
                        xlv_order.setBackgroundResource(R.drawable.listview_no_order);
                    } else {
                        xlv_order.setBackgroundResource(android.R.color.transparent);
                    }
                    isLoaded = true;
                    break;
                }
                case AppConst.WHAT_SUCC_SUBMIT: {
                    if (thread == null || !thread.isAlive()) {
                        thread = new Thread(runnable);
                        thread.start();
                    }
                    String info = ComUtil.getInfo(msg);
                    AppUtil.tip(getActivity(),info);
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            orderDetails = OrderAPI.userList(type, filter);
            if (orderDetails == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);           //加载数据失败
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);                //加载数据成功
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getActivity().getApplication();
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
        filter.setMoneyTicket(0.0);
        filter.setCategoryname("");

        orderAdapter = new OrderAdapter(getActivity(), dataHolder);
        orderAdapter.setOperationOnClickListener(operationOnClickListener);
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
        if (appShare.map.containsKey("type.refresh")) {
            refresh = (boolean) appShare.map.get("type.refresh");
            appShare.map.remove("type.refresh");
            if (refresh) {
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

    private View.OnClickListener operationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OperaData operaData = new OperaData();
            operaData.operation = (com.subang.domain.Order.State) v.getTag(R.id.key_operation);
            operaData.orderid = (Integer) v.getTag(R.id.key_id);
            switch (operaData.operation) {
                case canceled: {
                    if (operaThread == null || !operaThread.isAlive()) {
                        operaThread = new OperaThread(operaData);
                        operaThread.start();
                    }
                    break;
                }
                case paid: {
                    Intent intent = new Intent(getActivity(), PayActivity.class);
                    intent.putExtra("orderid", operaData.orderid);
                    startActivity(intent);
                    break;
                }
                case delivered: {
                    if (operaThread == null || !operaThread.isAlive()) {
                        operaThread = new OperaThread(operaData);
                        operaThread.start();
                    }
                    break;
                }
                case remarked: {
                    Intent intent = new Intent(getActivity(), RemarkActivity.class);
                    intent.putExtra("orderid", operaData.orderid);
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private static class OperaData implements Serializable {
        public Order.State operation;
        public Integer orderid;
    }

    private class OperaThread extends Thread {

        OperaData operaData;

        public OperaThread(OperaData operaData) {
            super();
            this.operaData = operaData;
        }

        @Override
        public void run() {
            Result result;
            Message msg;
            AppUtil.confApi(getActivity());
            switch (operaData.operation) {
                case canceled: {
                    result = OrderAPI.cancel(operaData.orderid);
                    if (result == null) {
                        handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                        return;
                    }
                    msg = ComUtil.getMessage(AppConst.WHAT_SUCC_SUBMIT, "订单取消成功。");
                    handler.sendMessage(msg);
                    break;
                }
                case delivered: {
                    result = OrderAPI.deliver(operaData.orderid);
                    if (result == null) {
                        handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                        return;
                    }
                    msg = ComUtil.getMessage(AppConst.WHAT_SUCC_SUBMIT, "订单送达成功。");
                    handler.sendMessage(msg);
                    break;
                }
            }
        }
    }
}
