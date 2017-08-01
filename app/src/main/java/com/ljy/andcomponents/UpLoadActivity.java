package com.ljy.andcomponents;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.ljy.expandwebview.DefaultDownListener;
import com.ljy.expandwebview.ProgressWebView;

public class UpLoadActivity extends AppCompatActivity {
    private ProgressWebView progressWebView;
    private ProgressWebView.DefaultWebChromeClient webChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_load);
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        progressWebView = new ProgressWebView(this, container, 2);
        webChromeClient = new ProgressWebView.DefaultWebChromeClient(progressWebView.getWebProgress(), this);
        progressWebView.setDefaultWebChromeClient(webChromeClient);
        progressWebView.setDownloadListener(new DefaultDownListener(this, R.mipmap.ic_launcher));
        String url = "http://ios.youchang88.com/cars/insurance/index?user_id=1001163";
        progressWebView.loadUrl(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        webChromeClient.uploadFileSelectCallBack(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
