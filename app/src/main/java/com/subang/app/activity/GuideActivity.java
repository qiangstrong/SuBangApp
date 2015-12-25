package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.subang.app.bean.AppEtc;
import com.subang.app.helper.ImagePagerAdapter;
import com.subang.app.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity {

    private static final int NUM_GUIDE = 3;

    private ViewPager vp_guide;
    private ImageView iv_dot1, iv_dot2, iv_dot3, iv_start;

    private List<ImageView> guideItems;

    private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0: {
                    iv_dot1.setImageResource(R.drawable.guide_dot_current);
                    iv_dot2.setImageResource(R.drawable.guide_dot_default);
                    iv_dot3.setImageResource(R.drawable.guide_dot_default);
                    iv_start.setVisibility(View.INVISIBLE);
                    break;
                }
                case 1: {
                    iv_dot1.setImageResource(R.drawable.guide_dot_default);
                    iv_dot2.setImageResource(R.drawable.guide_dot_current);
                    iv_dot3.setImageResource(R.drawable.guide_dot_default);
                    iv_start.setVisibility(View.INVISIBLE);
                    break;
                }
                case 2: {
                    iv_dot1.setImageResource(R.drawable.guide_dot_default);
                    iv_dot2.setImageResource(R.drawable.guide_dot_default);
                    iv_dot3.setImageResource(R.drawable.guide_dot_current);
                    iv_start.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppEtc appEtc = AppUtil.getEtc(GuideActivity.this);
        if (!appEtc.isFirst()) {
            Intent intent = new Intent(GuideActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_guide);
        findView();
        createItems();
        ImagePagerAdapter guideAdapter = new ImagePagerAdapter(guideItems);
        vp_guide.setAdapter(guideAdapter);
        vp_guide.setOnPageChangeListener(simpleOnPageChangeListener);
    }

    private void findView() {
        vp_guide = (ViewPager) findViewById(R.id.vp_guide);
        iv_dot1 = (ImageView) findViewById(R.id.iv_dot1);
        iv_dot2 = (ImageView) findViewById(R.id.iv_dot2);
        iv_dot3 = (ImageView) findViewById(R.id.iv_dot3);
        iv_start = (ImageView) findViewById(R.id.iv_start);
    }

    public void iv_start_onClick(View view) {
        AppEtc appEtc = new AppEtc();
        appEtc.setFirst(false);
        AppUtil.saveEtc(GuideActivity.this, appEtc);
        Intent intent = new Intent(GuideActivity.this, LoadActivity.class);
        startActivity(intent);
        finish();
    }

    private void createItems() {
        guideItems = new ArrayList<ImageView>(NUM_GUIDE);
        int[] guides = {R.drawable.guide_1_bg, R.drawable.guide_2_bg, R.drawable.guide_3_bg};
        ImageView guideItem;
        for (int i = 0; i < NUM_GUIDE; i++) {
            guideItem = new ImageView(GuideActivity.this);
            guideItem.setImageResource(guides[i]);
            guideItem.setScaleType(ImageView.ScaleType.FIT_XY);
            guideItems.add(guideItem);
        }
    }
}
