package com.subang.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;


public class TestActivity extends Activity {

    private ImageView iv_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findView();
        //iv_icon.setImageURI(Uri.parse(getFilesDir().getAbsolutePath() + "/" + "image/info/category/洗衣.png"));
        Bitmap bitmap = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/" + "image/info/category/洗衣.png");
        iv_icon.setImageBitmap(bitmap);

    }

    private void findView() {
        iv_icon=(ImageView)findViewById(R.id.iv_icon);
    }




}
