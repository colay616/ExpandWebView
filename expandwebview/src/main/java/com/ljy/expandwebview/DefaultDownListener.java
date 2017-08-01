package com.ljy.expandwebview;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;

import java.io.File;
import java.util.List;

/**
 * 描   述:webView 默认下载监听器
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/07/13 16:30
 * 参考:https://github.com/Justson/AgentWeb
 * 修改历史:
 */
public class DefaultDownListener implements DownloadListener {

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 0x001;
    private Context mContext;
    private int downIconRes = R.drawable.download;

    public DefaultDownListener(Context mContext) {
        this.mContext = mContext;
    }

    public DefaultDownListener(Context mContext, int downIconRes) {
        this.mContext = mContext;
        this.downIconRes = downIconRes;
    }

    private String tempUrl;
    private String tempContentDisposition;
    private long tempContentLength;

    private static int NoticationID = 1;

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        this.tempUrl = url;
        this.tempContentDisposition = contentDisposition;
        this.tempContentLength = contentLength;
        //filename=YYB.998886.0b0bb2d2ecf3d8742fcff9453052adfb.991653.apk
        //1.判断是否有存储权限
        if (AndPermission.hasPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //开始下载
            download();
        } else {
            //申请授权
            AndPermission.with(mContext)
                    .requestCode(WRITE_EXTERNAL_STORAGE_CODE)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .callback(this)
                    .start();
        }
    }


    @PermissionYes(value = WRITE_EXTERNAL_STORAGE_CODE)
    private void getCameraPermissionYes(@NonNull List<String> grantedPermissions) {
        download();
    }

    @PermissionNo(value = WRITE_EXTERNAL_STORAGE_CODE)
    private void getCameraPermissionNo(@NonNull List<String> grantedPermissions) {
        //权限被拒绝
        toast("下载失败:没有读写SD卡权限");
    }


    private void toast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 开始下载
     */
    private void download() {
        //判断下载服务是否正在进行
        File mFile = getFile(tempContentDisposition, tempUrl);
        new RealDownLoader(new DownLoadTask(NoticationID++, tempUrl, false, true, mContext, mFile, tempContentLength, R.drawable.download)).execute();
    }

    private File getFile(String contentDisposition, String url) {
        try {
            String filename = "";
            if (!TextUtils.isEmpty(contentDisposition) && contentDisposition.contains("filename") && !contentDisposition.endsWith("filename")) {

                int position = contentDisposition.indexOf("filename=");
                filename = contentDisposition.substring(position, contentDisposition.length());
            }
            if (TextUtils.isEmpty(filename) && !TextUtils.isEmpty(url) && !url.endsWith("/")) {

                int p = url.lastIndexOf("/");
                if (p != -1)
                    filename = url.substring(p + 1);
                if (filename.contains("?")) {
                    int index = filename.indexOf("?");
                    filename = filename.substring(0, index);

                }
            }

            if (TextUtils.isEmpty(filename)) {

                filename = System.currentTimeMillis() + "";
            }

            LogUtils.d("file:" + filename);
            File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

            if (!mFile.exists())
                mFile.createNewFile();
            return mFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
