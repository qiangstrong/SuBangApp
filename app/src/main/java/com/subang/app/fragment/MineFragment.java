package com.subang.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.InfoAPI;
import com.subang.api.UserAPI;
import com.subang.app.activity.R;
import com.subang.app.fragment.face.OnFrontListener;
import com.subang.app.util.AppUtil;
import com.subang.domain.Info;
import com.subang.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MineFragment extends Fragment implements OnFrontListener {

    private static final int NUM_ACTION = 6;
    private static final int NO_LINE = 0;
    private static final int YES_LINE = 1;

    private TextView tv_cellnum, tv_recharge, tv_money, tv_score, tv_phone;
    private ListView lv_action;

    private SimpleAdapter actionSimpleAdapter;

    private Thread thread;
    private User user;
    private Info info;
    private List<Map<String, Object>> actionItems;

    private boolean isUserLoaded = false;
    private boolean isInfoLoaded = false;

    private View.OnClickListener rechargeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener moneyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener scoreOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private AdapterView.OnItemClickListener actionOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    private SimpleAdapter.ViewBinder actionViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.v_line) {
                int line = (int) data;
                if (line == YES_LINE) {
                    view.setVisibility(View.VISIBLE);
                }else {
                    view.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    tv_cellnum.setText(user.getCellnum());
                    tv_money.setText(user.getMoney().toString() + "元");
                    tv_score.setText(user.getScore().toString());
                    isUserLoaded = true;
                    break;
                }
                case 2: {
                    tv_phone.setText("客服 " + info.getPhone());
                    isInfoLoaded = true;
                    break;
                }
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi();
            user = UserAPI.get();
            if (user != null) {
                handler.sendEmptyMessage(1);
            }
            info = InfoAPI.get();
            if (info != null) {
                handler.sendEmptyMessage(2);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createItems();
        actionSimpleAdapter = new SimpleAdapter(getActivity(), actionItems, R.layout.mine_item,
                new String[]{"icon", "text", "line"}, new int[]{R.id.iv_icon, R.id.tv_text, R.id.v_line});
        actionSimpleAdapter.setViewBinder(actionViewBinder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        findView(view);

        tv_recharge.setOnClickListener(rechargeOnClickListener);
        tv_money.setOnClickListener(moneyOnClickListener);
        tv_score.setOnClickListener(scoreOnClickListener);

        lv_action.setAdapter(actionSimpleAdapter);
        lv_action.setOnItemClickListener(actionOnItemClickListener);
        return view;
    }

    @Override
    public void onFront() {
        if (isUserLoaded && isInfoLoaded) {
            return;
        }
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView(View view) {
        tv_cellnum = (TextView) view.findViewById(R.id.tv_cellnum);
        tv_recharge = (TextView) view.findViewById(R.id.tv_recharge);
        tv_money = (TextView) view.findViewById(R.id.tv_money);
        tv_score = (TextView) view.findViewById(R.id.tv_score);
        lv_action = (ListView) view.findViewById(R.id.lv_action);
        tv_phone = (TextView) view.findViewById(R.id.tv_phone);
    }

    private void createItems() {
        actionItems = new ArrayList<Map<String, Object>>(NUM_ACTION);
        int[] icons = {R.drawable.mine_address_icon, R.drawable.mine_ticket_icon, R.drawable.mine_score_icon,
                R.drawable.mine_faq_icon, R.drawable.mine_feedback_icon, R.drawable.mine_more_icon};
        String[] texts = {"常用地址", "优惠券", "积分商城", "常见问题", "意见反馈", "更多"};
        Map<String, Object> actionItem;
        for (int i = 0; i < NUM_ACTION; i++) {
            actionItem = new HashMap<String, Object>();
            actionItem.put("icon", icons[i]);
            actionItem.put("text", texts[i]);
            actionItem.put("line", NO_LINE);
            actionItems.add(actionItem);
        }
        actionItems.get(2).put("line", YES_LINE);
        actionItems.get(5).put("line", YES_LINE);
    }
}
