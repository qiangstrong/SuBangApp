package com.subang.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.subang.app.util.AppConst;
import com.subang.app.util.AppUtil;
import com.subang.app.util.ComUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class TestActivity extends Activity {

    private IWXAPI wxapi;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        wxapi = WXAPIFactory.createWXAPI(this, AppConst.APP_ID, true);
        if (!wxapi.registerApp(AppConst.APP_ID)) {
            AppUtil.tip(TestActivity.this, "没有找到微信客户端。");
            this.finish();
            return;
        }
    }

    public void onClick(View view){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "http://movie.douban.com/subject/25785114";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "互联网之子";
        msg.description = "互联网之子";
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subang_icon);
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        msg.thumbData = ComUtil.bmpToByteArray(bitmap, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        wxapi.sendReq(req);
//
//        String text="互联网之子";
//        WXTextObject textObj = new WXTextObject();
//        textObj.text = text;
//
//        // 用WXTextObject对象初始化一个WXMediaMessage对象
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = textObj;
//        msg.description = text;
//
//        // 构造一个Req
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = String.valueOf(System.currentTimeMillis());
//        req.message = msg;
//        req.scene = SendMessageToWX.Req.WXSceneSession;
//
//        // 调用api接口发送数据到微信
//        wxapi.sendReq(req);
    }

    public void onClick1(View view){
//        ImageView iv_qrcode=(ImageView)findViewById(R.id.iv_qrcode);
//        String url="http://movie.douban.com/subject/25785114";
//        Bitmap bitmap=ComUtil.createQRImage(url);
//        iv_qrcode.setImageBitmap(bitmap);


    }


}
