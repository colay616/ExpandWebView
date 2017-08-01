package com.ljy.andcomponents;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.ljy.expandwebview.DefaultDownListener;
import com.ljy.expandwebview.ProgressWebView;

public class PayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        ProgressWebView progressWebView = new ProgressWebView(this, container, 2);
        //progressWebView.setDefaultWebClient(new ProgressWebView.DefaultWebViewClient());
        ProgressWebView.DefaultWebChromeClient webChromeClient = new ProgressWebView.DefaultWebChromeClient(progressWebView.getWebProgress(), this);
        progressWebView.setDefaultWebChromeClient(webChromeClient);
        progressWebView.setDownloadListener(new DefaultDownListener(this, R.mipmap.ic_launcher));
        String url = "HTTPS://QR.ALIPAY.COM/FKX01765CJRLKFJA53GW95";
        progressWebView.loadUrl(url);
    }
}
