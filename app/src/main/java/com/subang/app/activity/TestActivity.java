package com.subang.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.subang.applib.view.XListView;

import java.util.ArrayList;


public class TestActivity extends Activity implements XListView.IXListViewListener {
    private XListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> items = new ArrayList<String>();
    private Handler mHandler;

    private int count = 4;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        geneItems();
        mListView = (XListView) findViewById(R.id.xListView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mListView.setAdapter(mAdapter);
		mListView.setPullLoadEnable(false);
		mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(this);
        mHandler = new Handler();
    }

    private void geneItems() {
        items.clear();
        for (int i = 0; i < count; i++) {
            items.add("refresh cnt " + i);
        }
        count++;
    }



    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                geneItems();
                mAdapter.notifyDataSetChanged();
                mListView.stopRefresh();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {

    }
}
