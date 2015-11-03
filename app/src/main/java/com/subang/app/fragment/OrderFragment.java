package com.subang.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.subang.app.activity.R;
import com.subang.app.adapter.MyFragmentPagerAdapter;
import com.subang.app.fragment.face.OnFrontListener;
import com.subang.applib.view.AutoScrollViewPager;
import com.subang.util.WebConst;

import java.util.ArrayList;
import java.util.List;


public class OrderFragment extends Fragment implements OnFrontListener {

    private static final int NUM_FRAGMENT = 2;

    private RadioGroup rg_type;
    private RadioButton rb_undone, rb_done;
    private AutoScrollViewPager vp_order;

    private MyFragmentPagerAdapter fragmentPagerAdapter;

    RadioGroup.OnCheckedChangeListener typeOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_undone) {
                vp_order.setCurrentItem(0);
            } else {
                vp_order.setCurrentItem(1);
            }
        }
    };

    private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                rb_undone.setChecked(true);
            } else {
                rb_done.setChecked(true);
            }
            ((OnFrontListener)fragmentPagerAdapter.getItem(position)).onFront();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Fragment> fragments = new ArrayList<Fragment>(NUM_FRAGMENT);

        Bundle args = new Bundle();
        args.putInt("type", WebConst.ORDER_STATE_UNDONE);
        Fragment fragment = new TypeFragment();
        fragment.setArguments(args);
        fragments.add(fragment);

        args = new Bundle();
        args.putInt("type", WebConst.ORDER_STATE_DONE);
        fragment = new TypeFragment();
        fragment.setArguments(args);
        fragments.add(fragment);
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), fragments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        findView(view);
        rg_type.setOnCheckedChangeListener(typeOnCheckedChangeListener);

        vp_order.setAdapter(fragmentPagerAdapter);
        vp_order.setOnPageChangeListener(simpleOnPageChangeListener);
        vp_order.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_TO_PARENT);
        return view;
    }

    @Override
    public void onFront() {
        int position=vp_order.getCurrentItem();
        ((OnFrontListener)fragmentPagerAdapter.getItem(position)).onFront();
    }

    private void findView(View view) {
        rg_type = (RadioGroup) view.findViewById(R.id.rg_type);
        rb_undone = (RadioButton) view.findViewById(R.id.rb_undone);
        rb_done = (RadioButton) view.findViewById(R.id.rb_done);
        vp_order = (AutoScrollViewPager) view.findViewById(R.id.vp_order);
    }

}
