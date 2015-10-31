package com.subang.app.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.PriceAPI;
import com.subang.api.RegionAPI;
import com.subang.api.SubangAPI;
import com.subang.app.activity.R;
import com.subang.app.adapter.ImagePagerAdapter;
import com.subang.app.util.AppUtil;
import com.subang.applib.view.AutoScrollViewPager;
import com.subang.domain.Category;
import com.subang.domain.City;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment {

    private static final int NUM_BANNER = 3;
    private static final int NUM_CATEGORY_DEFAULT = 2;
    private static final int NUM_INFO = 2;

    private TextView tv_location;
    private AutoScrollViewPager vp_banner;
    private CirclePageIndicator pi_banner;
    private GridView gv_category;
    private GridView gv_info;

    private City city;
    private List<ImageView> bannerItems;
    private List<Map<String, Object>> categoryItems;
    private List<Map<String, Object>> infoItems;

    private boolean isLoaded = false;

    private View.OnClickListener locationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private AdapterView.OnItemClickListener categoryOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!isLoaded) {
                return;
            }

        }
    };
    private AdapterView.OnItemClickListener infoOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    private SimpleAdapter.ViewBinder categoryViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getClass() == ImageView.class && data.getClass() == Bitmap.class) {
                ((ImageView) view).setImageBitmap((Bitmap) data);
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (tv_location != null) {
                tv_location.setText(city.getName());
            }
            if (gv_category != null) {
                SimpleAdapter categorySimpleAdapter = new SimpleAdapter(getActivity(), categoryItems, R.layout
                        .gridview_item_home_category, new
                        String[]{"icon", "name", "comment"}, new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_comment});
                categorySimpleAdapter.setViewBinder(categoryViewBinder);
                gv_category.setAdapter(categorySimpleAdapter);
                isLoaded = true;
            }
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            Integer cityid = RegionAPI.getCityid();
            if (cityid == null) {
                return;
            }
            city = RegionAPI.getCity(cityid);
            if (city == null) {
                return;
            }
            List<Category> categorys = PriceAPI.listcategory(cityid, null);
            if (categorys == null) {
                return;
            }
            categoryItems = new ArrayList<Map<String, Object>>();
            Map<String, Object> categoryItem;
            for (Category category : categorys) {
                categoryItem = new HashMap<String, Object>();
                Bitmap bitmap = BitmapFactory.decodeFile(SubangAPI.BASE_PATH + category.getIcon());
                categoryItem.put("icon", bitmap);
                categoryItem.put("name", category.getName());
                categoryItem.put("comment", category.getComment());
                categoryItems.add(categoryItem);
            }
            handler.sendEmptyMessage(1);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createItems();
        new Thread(runnable).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        findView(view);

        if (city != null) {
            tv_location.setText(city.getName());
        }
        tv_location.setOnClickListener(locationOnClickListener);

        vp_banner.setAdapter(new ImagePagerAdapter(bannerItems));
        vp_banner.setInterval(2000);
        vp_banner.startAutoScroll();
        pi_banner.setViewPager(vp_banner);

        SimpleAdapter categorySimpleAdapter = new SimpleAdapter(getActivity(), categoryItems, R.layout
                .gridview_item_home_category, new
                String[]{"icon", "name", "comment"}, new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_comment});
        categorySimpleAdapter.setViewBinder(categoryViewBinder);
        gv_category.setAdapter(categorySimpleAdapter);
        gv_category.setOnItemClickListener(categoryOnItemClickListener);

        gv_info.setAdapter(new SimpleAdapter(getActivity(), infoItems, R.layout.gridview_item_home_info, new
                String[]{"text"}, new int[]{R.id.tv_intro}));
        gv_info.setOnItemClickListener(infoOnItemClickListener);

        return view;
    }

    private void findView(View view) {
        tv_location = (TextView) view.findViewById(R.id.tv_loction);
        vp_banner = (AutoScrollViewPager) view.findViewById(R.id.vp_banner);
        pi_banner = (CirclePageIndicator) view.findViewById(R.id.pi_banner);
        gv_category = (GridView) view.findViewById(R.id.gv_category);
        gv_info = (GridView) view.findViewById(R.id.gv_info);

    }

    private void createItems() {
        bannerItems = new ArrayList<ImageView>(NUM_BANNER);
        ImageView bannerItem = new ImageView(getActivity());
        bannerItem.setImageResource(R.drawable.banner_1);
        bannerItems.add(bannerItem);
        bannerItem = new ImageView(getActivity());
        bannerItem.setImageResource(R.drawable.banner_2);
        bannerItems.add(bannerItem);
        bannerItem = new ImageView(getActivity());
        bannerItem.setImageResource(R.drawable.banner_3);
        bannerItems.add(bannerItem);

        categoryItems = new ArrayList<Map<String, Object>>(NUM_CATEGORY_DEFAULT);
        Map<String, Object> categoryItem = new HashMap<String, Object>();
        categoryItem.put("icon", R.drawable.home_gridview_default);
        categoryItem.put("name", "");
        categoryItem.put("comment", "");
        categoryItems.add(categoryItem);
        categoryItem = new HashMap<String, Object>(categoryItem);
        categoryItems.add(categoryItem);

        infoItems = new ArrayList<Map<String, Object>>(NUM_INFO);
        Map<String, Object> infoItem = new HashMap<String, Object>();
        infoItem.put("text", "服务介绍");
        infoItems.add(infoItem);
        infoItem = new HashMap<String, Object>();
        infoItem.put("text", "服务范围");
        infoItems.add(infoItem);
    }

}
