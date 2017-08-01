package com.ljy.andcomponents;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.ljy.expandwebview.DefaultDownListener;
import com.ljy.expandwebview.ProgressWebView;
/**
 * 描   述:DownLoadActivity:文件下载
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/8/1 14:54
 * 修改历史:
 */
public class DownLoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        ProgressWebView progressWebView = new ProgressWebView(this, container, 2);
        //progressWebView.setDefaultWebClient(new ProgressWebView.DefaultWebViewClient());
        ProgressWebView.DefaultWebChromeClient webChromeClient = new ProgressWebView.DefaultWebChromeClient(progressWebView.getWebProgress(), this);
        progressWebView.setDefaultWebChromeClient(webChromeClient);
        progressWebView.setDownloadListener(new DefaultDownListener(this, R.mipmap.ic_launcher));
        String url = "http://a.app.qq.com/o/simple.jsp?pkgname=com.zpkj.pay";
        progressWebView.loadUrl(url);
    }
}
