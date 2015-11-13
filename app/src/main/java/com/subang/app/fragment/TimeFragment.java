package com.subang.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.subang.app.activity.R;
import com.subang.util.TimeUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TimeFragment extends Fragment {

    private TimeUtil.Option dateOption;

    private GridView gv_time;

    private OnResultListener onResultListener;
    private SimpleAdapter timeSimpleAdapter;

    private List<TimeUtil.Option> timeOptions;

    private AdapterView.OnItemClickListener timeOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onResultListener.onResult(view, timeOptions.get(position));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onResultListener = (OnResultListener) getActivity();
        if (getArguments().containsKey("date")) {
            dateOption = (TimeUtil.Option) getArguments().getSerializable("date");
        }

        timeOptions = TimeUtil.getTimeOptions((Date) dateOption.getValue());
        List<Map<String, Object>> timeItems = new ArrayList<Map<String, Object>>(timeOptions.size());
        Map<String, Object> timeItem;
        for (TimeUtil.Option timeOption : timeOptions) {
            timeItem = new HashMap<String, Object>();
            timeItem.put("text", timeOption.getText());
            timeItems.add(timeItem);
        }

        timeSimpleAdapter = new SimpleAdapter(getActivity(), timeItems, R.layout.item_time, new String[]{"text"}, new
                int[]{R.id.tv_time});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time, container, false);
        findView(view);
        gv_time.setAdapter(timeSimpleAdapter);
        gv_time.setOnItemClickListener(timeOnItemClickListener);
        return view;
    }

    private void findView(View view) {
        gv_time = (GridView) view.findViewById(R.id.gv_time);
    }

    public static interface OnResultListener {
        public void onResult(View view, TimeUtil.Option timeOption);
    }

}
