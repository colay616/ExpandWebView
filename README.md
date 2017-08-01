# 感谢:
#### 感谢大牛 <font size='5'><strong>http://www.jianshu.com/u/a52f305fac1c</strong></font> 的开源项目: https://github.com/Justson/AgentWeb ,我是根据我项目的需求,在他的代码基础上(甚至很大一部分都是他的源码拷贝的)实现的
### 测试app下载 <br/>
https://github.com/lijiayan2015/ExpandWebView/blob/master/app-debug.apk
# 主要功能:
1.带进度条的webView.<br/>
2.图片上传,上传的时候可以选择图片,也可以相机拍照.<br/>
3.文件下载,下载完成后可直接点击下载通知打开,其中若是下载的apk,可直接点击下载通知进行安装.<br/>
4.在html中有打开app的按钮的话,如果本地已经安装了目标app,可直接打开app,在源码中我有调用支付宝支付的demo,可下载源码进行体验.<br/>
# 用法
1.在项目的project的 build.gradle下添加:maven { url 'https://jitpack.io' }<br/>
````
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
````
2.在项目的app的 build.gradle下添加依赖:compile 'com.github.lijiayan2015:ExpandWebView:expandWeb1.0' <br/>
3.在AndroidManifest.xml文件中配置所需权限
````
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
````
4.配置AndroidManifest.xml的application节点下配置FileProvider 如下:
````
<provider
    android:name="com.ljy.expandwebview.WebFileProvider"
    android:authorities="${applicationId}.WebFileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/web_download_paths" />
 </provider>
 ````
 5.配置一个广播接收者(用于点击下载进度条打开文件),如果是自定义DownloadListener的话,可以使用PendingIntent方法解决,因为我在项目中使用PendingInten方式老是打开文件失败,所以就换成广播接收者来处理通知的点击事件了.配置如下:<br/>
 ````
<receiver android:name="com.ljy.expandwebview.OpenFileReceiver">
      <intent-filter>
          <action android:name="com.ljy.expandwebview.OpenFileReceiver" />
      </intent-filter>
 </receiver>
````
6.通过以上步骤就算配置完成了,看着挺麻烦,但是也就粘贴下代码,很简单的.接下来在Activity中使用:<br/>
布局文件:<br/>
````
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
````
java代码:
````
@Override
protected void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
   setContentView(R.layout.activity_pay);
   FrameLayout container = (FrameLayout) findViewById(R.id.container);
   ProgressWebView progressWebView = new ProgressWebView(this, container, 2);
   ProgressWebView.DefaultWebChromeClient 
                      webChromeClient = new  ProgressWebView.DefaultWebChromeClient(
                      progressWebView.getWebProgress(), this);
   progressWebView.setDefaultWebChromeClient(webChromeClient);
   progressWebView.setDownloadListener(new DefaultDownListener(this, R.mipmap.ic_launcher));
   String url = "HTTPS://QR.ALIPAY.COM/FKX01765CJRLKFJA53GW95";
   progressWebView.loadUrl(url);
}
````
7.如果是有需要上传图片的需求,在Activity中的代码如下:
````
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
````

 
