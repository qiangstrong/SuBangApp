package com.subang.app.helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.subang.applib.util.RecyclingPagerAdapter;

import java.util.List;

/**
 * Created by Qiang on 2015/10/30.
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

    private List<ImageView> imageViews;

    public ImagePagerAdapter(List<ImageView> imageViews) {
        this.imageViews = imageViews;
    }

    @Override
    public int getCount() {
        return imageViews.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        return imageViews.get(position);
    }
}
