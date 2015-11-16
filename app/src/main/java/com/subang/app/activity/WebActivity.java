package com.subang.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.subang.util.WebConst;

public class WebActivity extends Activity {

    private String title, url;

    private TextView tv_title;
    private WebView wv_web;
    private ProgressBar pb_web;

    private WebChromeClient webChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            pb_web.setProgress(progress);
            if (progress == 100) {
                pb_web.setVisibility(View.GONE);
            }
        }
    };

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(WebConst.HOST_NAME)) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        findView();
        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        tv_title.setText(title);

        WebSettings webSettings = wv_web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv_web.setWebChromeClient(webChromeClient);
        wv_web.setWebViewClient(webViewClient);
        wv_web.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_web.canGoBack()) {
            wv_web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void findView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        wv_web = (WebView) findViewById(R.id.wv_web);
        pb_web = (ProgressBar) findViewById(R.id.pb_web);
    }

    public void iv_back_onClick(View view) {
        finish();
    }
}
