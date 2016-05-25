package com.subang.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.subang.api.InfoAPI;
import com.subang.app.util.AppConst;
import com.subang.app.util.AppShare;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.subang.domain.Info;
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

public class ShareActivity extends Activity {

    private static final int NUM_SHARE = 2;

    private AppShare appShare;
    private IWXAPI wxapi;
    private Integer orderid;

    private GridView gv_share;
    private ImageView iv_qrcode;
    private TextView tv_rule;

    private Thread thread;
    private Info info;
    private List<Map<String, Object>> shareItems;
    private String shareUrl;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConst.WHAT_NETWORK_ERR: {
                    AppUtil.networkTip(ShareActivity.this);
                    break;
                }
                case AppConst.WHAT_SUCC_LOAD: {
                    setRule();
                    break;
                }
            }

        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AppUtil.confApi(ShareActivity.this);
            info= InfoAPI.get(null);
            if (info == null) {
                handler.sendEmptyMessage(AppConst.WHAT_NETWORK_ERR);
                return;
            }
            handler.sendEmptyMessage(AppConst.WHAT_SUCC_LOAD);
        }
    };

    private AdapterView.OnItemClickListener shareOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!wxapi.registerApp(AppConst.APP_ID)) {
                AppUtil.tip(ShareActivity.this, "没有找到微信客户端。");
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

            appShare.map.put(req.transaction,orderid);

            wxapi.sendReq(req);
            ShareActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        findView();
        orderid = getIntent().getIntExtra("orderid", 0);
        appShare = (AppShare) getApplication();
        wxapi = WXAPIFactory.createWXAPI(this, AppConst.APP_ID, true);
        createItems();
        gv_share.setAdapter(new SimpleAdapter(ShareActivity.this, shareItems, R.layout.item_share, new
                String[]{"icon", "text"}, new int[]{R.id.iv_icon, R.id.tv_text}));
        gv_share.setOnItemClickListener(shareOnItemClickListener);

        AppUtil.conf(ShareActivity.this);
        shareUrl= SuUtil.getSharePath();
        Bitmap bitmap= ComUtil.createQRImage(shareUrl,500,500);
        iv_qrcode.setImageBitmap(bitmap);

        info=new Info();
        info.setShareMoney(1.0);
        setRule();

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void findView() {
        gv_share = (GridView) findViewById(R.id.gv_share);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        tv_rule = (TextView) findViewById(R.id.tv_rule);
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

    private void setRule(){
        tv_rule.setText(getString(R.string.share_rule,info.getShareMoney()));
    }
}

