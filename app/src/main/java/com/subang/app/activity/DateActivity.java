package com.subang.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.subang.app.adapter.MyFragmentPagerAdapter;
import com.subang.app.fragment.TimeFragment;
import com.subang.util.TimeUtil;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class DateActivity extends Activity implements TimeFragment.OnResultListener {

    private TitlePageIndicator pi_date;
    private ViewPager vp_date;

    private List<TimeUtil.Option> dateOptions;
    private View selectedView;
    private TimeUtil.Option selectedDateOption, selectedTimeOption;
    private boolean isSelected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        findView();
        dateOptions =TimeUtil.getDateOptions();

        List<Fragment> fragments=new ArrayList<Fragment>(dateOptions.size());
        List<String> titles=new ArrayList<String>(dateOptions.size());
        TimeFragment fragment;
        Bundle args;
        for (TimeUtil.Option dateOption: dateOptions) {
            args = new Bundle();
            args.putSerializable("date",dateOption);
            fragment=new TimeFragment();
            fragment.setArguments(args);
            fragments.add(fragment);

            titles.add(dateOption.getText());
        }

        MyFragmentPagerAdapter fragmentPagerAdapter=new MyFragmentPagerAdapter(getFragmentManager(),fragments,titles);
        vp_date.setAdapter(fragmentPagerAdapter);
        pi_date.setViewPager(vp_date);
    }

    private void findView() {
        pi_date = (TitlePageIndicator) findViewById(R.id.pi_date);
        vp_date = (ViewPager) findViewById(R.id.vp_date);
    }

    @Override
    public void onResult(View view, TimeUtil.Option timeOption) {
        isSelected=true;
        int position = vp_date.getCurrentItem();
        this.selectedDateOption = dateOptions.get(position);
        if (selectedView != null) {
            selectedView.setBackgroundResource(R.drawable.white_btn_bg);
        }
        view.setBackgroundResource(R.drawable.blue_btn_bg);
        this.selectedView = view;
        this.selectedTimeOption = timeOption;
    }

    public void tv_cancel_onClick(View view){
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }

    public void tv_ok_onClick(View view){
        if (!isSelected){
            Toast toast = Toast.makeText(DateActivity.this,"请选择取件时间。", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        Intent intent=getIntent();
        intent.putExtra("date",selectedDateOption);
        intent.putExtra("time",selectedTimeOption);
        setResult(RESULT_OK, intent);
        finish();
    }
}
