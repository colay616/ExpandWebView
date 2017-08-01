package com.ljy.expandwebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * 描   述:带进度条的WebView
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/08/01 13:42
 * 参考源码:https://github.com/Justson/AgentWeb
 * 修改历史:
 */
public class ProgressWebView {

    private Context mContext;
    private ViewGroup containner;
    private WebView mWebView;
    private WebProgress mWebProgress;

    private WebSettings mWebSettings;

    private int progressHeightDp = 2;


    /**
     * @param mContext         上下文
     * @param containner       webView的父控件
     * @param progressHeightDp 进度条的高度  单位dp
     */
    public ProgressWebView(Context mContext, ViewGroup containner, int progressHeightDp) {
        this.mContext = mContext;
        if (containner == null) throw new IllegalArgumentException("The containner can't be null.");
        this.containner = containner;
        if (progressHeightDp >= 0) this.progressHeightDp = progressHeightDp;
        init();
    }

    private void init() {
        mWebView = new WebView(mContext);
        mWebProgress = new WebProgress(mContext);
        setUpSettings();
        setUpWebViewWidthProgress();

    }

    public void setDefaultWebChromeClient(DefaultWebChromeClient webChromeClient) {
        mWebView.setWebChromeClient(webChromeClient);
    }

    public void setDefaultWebClient(DefaultWebViewClient webViewClient) {
        mWebView.setWebViewClient(webViewClient);
    }

    /**
     * 设置webView与进度条的UI
     */
    private void setUpWebViewWidthProgress() {
        FrameLayout frameLayout = new FrameLayout(mContext);
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(-1, -1);
        frameLayout.addView(mWebView, mLayoutParams);
        FrameLayout.LayoutParams lp = null;
        lp = new FrameLayout.LayoutParams(-2, WebUtils.dp2px(mContext, progressHeightDp));
        mWebProgress.setColor(Color.parseColor("#00A3FF"));
        lp.gravity = Gravity.TOP;
        frameLayout.addView(mWebProgress, lp);
        mWebView.setWebChromeClient(new DefaultWebChromeClient(mWebProgress, mContext));
        this.containner.addView(frameLayout, new ViewGroup.LayoutParams(-1, -1));
    }

    /**
     * WebSettings的基本设置
     */
    @SuppressWarnings("all")
    private void setUpSettings() {
        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebViewClient(new DefaultWebViewClient());
    }

    public WebProgress getWebProgress() {
        return mWebProgress;
    }

    public static class DefaultWebChromeClient extends WebChromeClient {

        //================================上传文件============================
        private String mCM;
        private ValueCallback<Uri> mUM;
        private ValueCallback<Uri[]> mUMA;
        private final static int FCR = 1;
        private OnStartUpLoadListener onStartUpLoadListener;
        private Context mContext;
        //================================上传文件===========================

        private WebProgress mWebProgress;

        public DefaultWebChromeClient(WebProgress mWebProgress, Context mContext) {
            this.mWebProgress = mWebProgress;
            this.mContext = mContext;
        }

        /**
         * 设置进度条
         *
         * @param view
         * @param newProgress
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (mWebProgress != null) {
                if (newProgress == 100) {
                    mWebProgress.hide();
                }
                mWebProgress.setProgress(newProgress);
            }
        }

        //======================================上传文件==============================
        //For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ((Activity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
        }

        // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ((Activity) mContext).startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FCR);
        }

        //For Android 4.1+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ((Activity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
        }

        //For Android 5.0+
        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
            if (mUMA != null) {
                mUMA.onReceiveValue(null);
            }
            mUMA = filePathCallback;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCM);
                } catch (IOException ex) {
                    Log.e(ProgressWebView.class.getSimpleName(), "Image file creation failed", ex);
                }
                if (photoFile != null) {
                    mCM = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            ((Activity) mContext).startActivityForResult(chooserIntent, FCR);
            return true;
        }

        private File createImageFile() throws IOException {
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "img_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        }
        //======================================上传文件==============================

        /**
         * 上传文件时选择文件返回
         *
         * @param requestCode
         * @param resultCode
         * @param intent
         */
        public void uploadFileSelectCallBack(int requestCode, int resultCode, Intent intent) {
            if (Build.VERSION.SDK_INT >= 21) {
                Uri[] results = null;
                //Check if response is positive
                if (resultCode == RESULT_OK) {
                    if (requestCode == FCR) {
                        if (null == mUMA) {
                            return;
                        }
                        if (intent == null) {
                            //Capture Photo if no image available
                            if (mCM != null) {
                                results = new Uri[]{Uri.parse(mCM)};
                            }
                        } else {
                            String dataString = intent.getDataString();
                            LogUtils.d("选择的文件是:" + dataString);
                            if (dataString != null) {
                                results = new Uri[]{Uri.parse(dataString)};
                                if (onStartUpLoadListener != null) {
                                    onStartUpLoadListener.start();
                                }
                            }
                        }
                    }
                }
                mUMA.onReceiveValue(results);
                mUMA = null;

            } else {
                if (requestCode == FCR) {
                    if (null == mUM) return;
                    Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                    LogUtils.d("选择的文件是:" + result.toString());
                    mUM.onReceiveValue(result);
                    mUM = null;
                    if (onStartUpLoadListener != null) {
                        onStartUpLoadListener.start();
                    }

                }
            }

        }


        /**
         * 上传文件开始的监听
         * 上传文件结束我没法监听,后期可能会加上
         */
        public static abstract class OnStartUpLoadListener {
            public abstract void start();
        }

        public void setOnStartUpLoadListener(OnStartUpLoadListener onStartUpLoadListener) {
            this.onStartUpLoadListener = onStartUpLoadListener;
        }

    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    public static class DefaultWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) { // 重写此方法可以让webview处理https请求
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtils.e("shouldOverrideUrlLoading: " + url);
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme)) {
                view.loadUrl(url);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Intent intent;
                        intent = Intent.parseUri(url,
                                Intent.URI_INTENT_SCHEME);
                        //intent.addCategory("android.intent.category.BROWSABLE");
                        intent.addCategory(intent.CATEGORY_BROWSABLE);
                        intent.setComponent(null);
                        intent.setSelector(null);
                        view.getContext().startActivity(intent);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Toast.makeText(view.getContext(), "打开应用失败,请确认是否安装了该应用", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            return true;
            //return super.shouldOverrideUrlLoading(view, url);
        }
    }

    //======================================上传文件==============================


    //======================================上传文件==============================


    public void onResume() {
        if (this.mWebView != null) {

            if (Build.VERSION.SDK_INT >= 11)
                this.mWebView.onResume();

            this.mWebView.resumeTimers();
        }
    }


    public void onPause() {
        if (this.mWebView != null) {
            this.mWebView.pauseTimers();
            if (Build.VERSION.SDK_INT >= 11)
                this.mWebView.onPause();
        }
    }


    public void onDestroyView() {
        if (mWebView == null)
            return;
        if (Looper.myLooper() != Looper.getMainLooper())
            return;
        mWebView.getHandler().removeCallbacksAndMessages(null);
        mWebView.removeAllViews();
        ((ViewGroup) mWebView.getParent()).removeView(mWebView);
        mWebView.setTag(null);
        mWebView.clearHistory();
        mWebView.destroy();
        mWebView = null;
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBack() {
        if (mWebView.canGoBack()) mWebView.goBack();
    }

    /**
     * 设置下载监听
     *
     * @param downloadListener
     */
    public void setDownloadListener(DownloadListener downloadListener) {
        if (downloadListener != null) {
            mWebView.setDownloadListener(downloadListener);
        }
    }
}