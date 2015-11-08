package com.subang.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.subang.app.adapter.MyFragmentPagerAdapter;
import com.subang.app.fragment.HomeFragment;
import com.subang.app.fragment.MineFragment;
import com.subang.app.fragment.OrderFragment;
import com.subang.app.util.AppShare;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int NUM_FRAGMENT = 3;

    private AppShare appShare;

    private ViewPager vp_main;
    private ImageView[] imageViews;

    private MyFragmentPagerAdapter fragmentPagerAdapter;

    private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            iv_onClick(imageViews[position]);
            //((OnFrontListener)fragmentPagerAdapter.getItem(position)).onFront();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare=(AppShare)getApplication();
        setContentView(R.layout.activity_main);
        findView();

        List<Fragment> fragments = new ArrayList<Fragment>(NUM_FRAGMENT);
        fragments.add(new HomeFragment());
        fragments.add(new OrderFragment());
        fragments.add(new MineFragment());

        fragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager(), fragments);
        vp_main.setAdapter(fragmentPagerAdapter);
        vp_main.setOnPageChangeListener(simpleOnPageChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Integer position;
        if (appShare.map.containsKey("main.position")){
            position=(Integer)appShare.map.get("main.position");
            appShare.map.remove("main.position");
            vp_main.setCurrentItem(position);
        }
    }

    private void findView() {
        vp_main = (ViewPager) findViewById(R.id.vp_main);
        imageViews = new ImageView[NUM_FRAGMENT];
        imageViews[0] = (ImageView) findViewById(R.id.iv_home);
        imageViews[1] = (ImageView) findViewById(R.id.iv_order);
        imageViews[2] = (ImageView) findViewById(R.id.iv_mine);
    }

    public void iv_onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_home: {
                imageViews[0].setImageResource(R.drawable.home_press_icon);
                imageViews[1].setImageResource(R.drawable.order_default_icon);
                imageViews[2].setImageResource(R.drawable.mine_default_icon);
                vp_main.setCurrentItem(0);
                break;
            }
            case R.id.iv_order: {
                imageViews[0].setImageResource(R.drawable.home_default_icon);
                imageViews[1].setImageResource(R.drawable.order_press_icon);
                imageViews[2].setImageResource(R.drawable.mine_default_icon);
                vp_main.setCurrentItem(1);
                break;
            }
            case R.id.iv_mine: {
                imageViews[0].setImageResource(R.drawable.home_default_icon);
                imageViews[1].setImageResource(R.drawable.order_default_icon);
                imageViews[2].setImageResource(R.drawable.mine_press_icon);
                vp_main.setCurrentItem(2);
                break;
            }
        }

    }

}
