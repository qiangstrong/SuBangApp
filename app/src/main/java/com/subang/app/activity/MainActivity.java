package com.subang.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.subang.app.fragment.HomeFragment;
import com.subang.app.fragment.MineFragment;
import com.subang.app.fragment.OrderFragment;
import com.subang.app.helper.MyFragmentPagerAdapter;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.util.WebConst;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengRegistrar;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int NUM_FRAGMENT = 3;

    private PushAgent pushAgent;
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

        //友盟消息推送
        pushAgent = PushAgent.getInstance(MainActivity.this);
        pushAgent.enable(umengRegisterCallback);
        pushAgent.onAppStart();
        PushAgent.getInstance(MainActivity.this).setMuteDurationSeconds(3);
        String device_token = UmengRegistrar.getRegistrationId(MainActivity.this);
        Log.e(AppConst.LOG_TAG, device_token);
        Log.e(AppConst.LOG_TAG, String.valueOf(pushAgent.isEnabled()));

        //友盟自动更新
        UmengUpdateAgent.update(this);
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
        imageViews[1] = (ImageView) findViewById(R.id.iv_info);
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
            case R.id.iv_info: {
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

    private IUmengRegisterCallback umengRegisterCallback = new IUmengRegisterCallback() {
        @Override
        public void onRegistered(String registrationId) {
            AppUtil.conf(MainActivity.this);

            pushAgent.setNoDisturbMode(0, 0, 0, 0);
            try {
                pushAgent.addAlias(AppConf.cellnum, WebConst.ALIAS_TYPE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(AppConst.LOG_TAG, "IUmengRegisterCallback");
        }
    };
}
