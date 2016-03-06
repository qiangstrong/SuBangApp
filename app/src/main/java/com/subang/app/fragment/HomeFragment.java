package com.subang.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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

import com.subang.api.ActivityAPI;
import com.subang.api.PriceAPI;
import com.subang.api.RegionAPI;
import com.subang.app.activity.AddOrderActivity;
import com.subang.app.activity.CityActivity;
import com.subang.app.activity.R;
import com.subang.app.activity.WebActivity;
import com.subang.app.fragment.face.OnFrontListener;
import com.subang.app.helper.ImagePagerAdapter;
import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.applib.view.AutoScrollViewPager;
import com.subang.domain.Banner;
import com.subang.domain.Category;
import com.subang.domain.City;
import com.subang.util.WebConst;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment implements OnFrontListener {

    private static final int NUM_BANNER_DEFAULT = 3;
    private static final int NUM_CATEGORY_DEFAULT = 2;
    private static final int NUM_INFO = 2;

    private static final int WHAT_BANNER = 1;
    private static final int WHAT_CITY = 2;
    private static final int WHAT_CATEGORY = 3;

    private AppShare appShare;

    private TextView tv_location;
    private AutoScrollViewPager vp_banner;
    private CirclePageIndicator pi_banner;
    private GridView gv_category;
    private GridView gv_info;

    private ImagePagerAdapter bannerAdapter;
    private SimpleAdapter categoryAdapter;

    private Thread thread,categoryThread;
    private List<Banner> banners;
    private City city;
    private List<Category> categorys;
    private List<ImageView> bannerItems;
    private List<Map<String, Object>> categoryItems;
    private List<Map<String, Object>> infoItems;

    private boolean isLoaded = false;

    private View.OnClickListener locationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), CityActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    private View.OnClickListener bannerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag(R.id.key_link);
            if (url!=null&&url.length()!=0){
                Intent intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        }
    };

    private AdapterView.OnItemClickListener categoryOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!isLoaded) {
                return;
            }
            Intent intent = new Intent(getActivity(), AddOrderActivity.class);
            Category category= categorys.get(position);
            if (!category.getValid()){
                return;
            }
            intent.putExtra("category", categorys.get(position));
            startActivity(intent);
        }
    };

    private AdapterView.OnItemClickListener infoOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: {
                    Intent intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("title", "服务介绍");
                    intent.putExtra("url", WebConst.HOST_URI + "content/weixin/info/serviceintro.htm");
                    startActivity(intent);
                    break;
                }
                case 1: {
                    if (!isLoaded) {
                        return;
                    }
                    Intent intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("title", "服务范围");
                    intent.putExtra("url", WebConst.HOST_URI + "weixin/region/scope.html?cityid=" + city.getId());
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private SimpleAdapter.ViewBinder categoryViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.iv_icon && data instanceof Bitmap) {
                ((ImageView) view).setImageBitmap((Bitmap) data);
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(getActivity());
                    break;
                }
                case WHAT_BANNER: {
                    bannerItems.clear();
                    ImageView bannerItem;
                    AppUtil.conf(getActivity());
                    for (Banner banner : banners) {
                        bannerItem = new ImageView(getActivity());
                        Bitmap bitmap = BitmapFactory.decodeFile(AppConf.basePath + banner.getIcon());
                        bannerItem.setImageBitmap(bitmap);
                        bannerItem.setTag(R.id.key_link, banner.getLink());
                        bannerItem.setOnClickListener(bannerOnClickListener);
                        bannerItems.add(bannerItem);
                    }
                    bannerAdapter = new ImagePagerAdapter(bannerItems);
                    vp_banner.setAdapter(bannerAdapter);
                    break;
                }
                case WHAT_CITY: {
                    tv_location.setText(city.getName());
                    if (categoryThread == null || !categoryThread.isAlive()) {
                        categoryThread = new Thread(categoryRunnable);
                        categoryThread.start();
                    }
                    break;
                }
                case WHAT_CATEGORY: {
                    categoryItems.clear();
                    Map<String, Object> categoryItem;
                    AppUtil.conf(getActivity());
                    for (Category category : categorys) {
                        categoryItem = new HashMap<String, Object>();
                        Bitmap bitmap = BitmapFactory.decodeFile(AppConf.basePath + category.getIcon());
                        categoryItem.put("icon", bitmap);
                        categoryItem.put("name", category.getName());
                        categoryItem.put("comment", category.getComment());
                        categoryItems.add(categoryItem);
                    }
                    categoryAdapter.notifyDataSetChanged();
                    isLoaded = true;
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            city = RegionAPI.getUserCity();
            if (city == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_CITY);

            banners = ActivityAPI.listBanner(null);
            if (banners == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_BANNER);
        }
    };

    private Runnable categoryRunnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(getActivity());
            categorys = PriceAPI.listcategory(city.getId(), null);
            if (categorys == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(WHAT_CATEGORY);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appShare = (AppShare) getActivity().getApplication();
        createItems();
        bannerAdapter = new ImagePagerAdapter(bannerItems);
        categoryAdapter = new SimpleAdapter(getActivity(), categoryItems, R.layout
                .item_category, new
                String[]{"icon", "name", "comment"}, new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_comment});
        categoryAdapter.setViewBinder(categoryViewBinder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        findView(view);
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }

        tv_location.setOnClickListener(locationOnClickListener);

        vp_banner.setAdapter(bannerAdapter);
        vp_banner.setInterval(2000);
        vp_banner.startAutoScroll();
        vp_banner.setOffscreenPageLimit(3);
        vp_banner.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_TO_PARENT);
        pi_banner.setViewPager(vp_banner);

        gv_category.setAdapter(categoryAdapter);
        gv_category.setOnItemClickListener(categoryOnItemClickListener);

        gv_info.setAdapter(new SimpleAdapter(getActivity(), infoItems, R.layout.item_info, new
                String[]{"text"}, new int[]{R.id.tv_intro}));
        gv_info.setOnItemClickListener(infoOnItemClickListener);

        return view;
    }

    @Override
    public void onFront() {
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refresh;
        if (appShare.map.containsKey("home.refresh")) {
            refresh = (boolean) appShare.map.get("home.refresh");
            appShare.map.remove("home.refresh");
            if (refresh) {
                isLoaded=false;
                if (thread == null || !thread.isAlive()) {
                    thread = new Thread(runnable);
                    thread.start();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            isLoaded = false;
            Bundle bundle = intent.getExtras();
            city = (City) bundle.get("city");
            handler.sendEmptyMessage(WHAT_CITY);
        }
    }

    private void findView(View view) {
        tv_location = (TextView) view.findViewById(R.id.tv_loction);
        vp_banner = (AutoScrollViewPager) view.findViewById(R.id.vp_banner);
        pi_banner = (CirclePageIndicator) view.findViewById(R.id.pi_banner);
        gv_category = (GridView) view.findViewById(R.id.gv_category);
        gv_info = (GridView) view.findViewById(R.id.gv_info);
    }

    private void createItems() {
        bannerItems = new ArrayList<ImageView>(NUM_BANNER_DEFAULT);
        ImageView bannerItem;
        for (int i = 0; i < NUM_BANNER_DEFAULT; i++) {
            bannerItem = new ImageView(getActivity());
            bannerItem.setImageResource(R.drawable.banner_default);
            bannerItems.add(bannerItem);
        }

        categoryItems = new ArrayList<Map<String, Object>>(NUM_CATEGORY_DEFAULT);
        Map<String, Object> categoryItem = new HashMap<String, Object>();
        categoryItem.put("icon", R.drawable.home_item_default);
        categoryItem.put("name", "");
        categoryItem.put("comment", "");
        for (int i = 0; i < NUM_CATEGORY_DEFAULT; i++) {
            categoryItem = new HashMap<String, Object>(categoryItem);
            categoryItems.add(categoryItem);
        }

        infoItems = new ArrayList<Map<String, Object>>(NUM_INFO);
        Map<String, Object> infoItem = new HashMap<String, Object>();
        infoItem.put("text", "服务介绍");
        infoItems.add(infoItem);
        infoItem = new HashMap<String, Object>();
        infoItem.put("text", "服务范围");
        infoItems.add(infoItem);
    }
}
