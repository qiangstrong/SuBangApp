package com.subang.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.subang.app.util.AppConf;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.util.SuUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromQrcodeActivity extends Activity {

    private static final int NUM_SHARE = 2;

    private IWXAPI wxapi;

    private GridView gv_share;
    private ImageView iv_qrcode;

    private List<Map<String, Object>> shareItems;
    private String shareUrl;

    private AdapterView.OnItemClickListener shareOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!wxapi.registerApp(AppConst.APP_ID)) {
                AppUtil.tip(PromQrcodeActivity.this, "没有找到微信客户端。");
                return;
            }

            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = shareUrl;
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = "速帮洗衣";
            msg.description = "速帮洗衣";
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subang_icon);
            bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            msg.thumbData = ComUtil.bmpToByteArray(bitmap, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = String.valueOf(System.currentTimeMillis());
            req.message = msg;
            req.scene = position==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
            wxapi.sendReq(req);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prom_qrcode);
        findView();
        wxapi = WXAPIFactory.createWXAPI(this, AppConst.APP_ID, true);
        createItems();
        gv_share.setAdapter(new SimpleAdapter(PromQrcodeActivity.this, shareItems, R.layout.item_share, new
                String[]{"icon", "text"}, new int[]{R.id.iv_icon, R.id.tv_text}));
        gv_share.setOnItemClickListener(shareOnItemClickListener);

        AppUtil.conf(PromQrcodeActivity.this);
        shareUrl=SuUtil.getPromPath(AppConf.cellnum);
        Bitmap bitmap= ComUtil.createQRImage(shareUrl,500,500);
        iv_qrcode.setImageBitmap(bitmap);
    }

    private void findView() {
        gv_share = (GridView) findViewById(R.id.gv_share);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
    }

    private void createItems() {
        shareItems = new ArrayList<Map<String, Object>>(NUM_SHARE);
        Map<String, Object> shareItem = new HashMap<String, Object>();
        shareItem.put("text", "微信好友");
        shareItem.put("icon", R.drawable.wechat_friend);
        shareItems.add(shareItem);
        shareItem = new HashMap<String, Object>();
        shareItem.put("text", "朋友圈");
        shareItem.put("icon", R.drawable.wechat_cycle);
        shareItems.add(shareItem);
    }
}
