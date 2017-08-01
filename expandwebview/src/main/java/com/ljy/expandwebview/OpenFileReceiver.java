package com.ljy.expandwebview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;

import static com.ljy.expandwebview.FileUtils.getFileUri;
import static com.ljy.expandwebview.FileUtils.getMimeType;

/**
 * 描   述:点击通知栏下载进度条(下载完成)后的操作
 * 这个操作本来是可以直接使用PendingIntent的完成,但是使用PendingIntent后
 * 打开系统的安装器安装失败,我不知道是怎么回事,所以用这种方法解决
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/07/18 14:54
 * 修改历史:
 */
public class OpenFileReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null
                && "com.ljy.expandwebview.OpenFileReceiver".equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                try {
                    //文件的绝对路径
                    String fileAbsPath = extras.getString("fileAdsPath");
                    File file = new File(fileAbsPath);
                    Intent mIntent = new Intent(Intent.ACTION_VIEW);
                    mIntent.setDataAndType(getFileUri(context, file), getMimeType(file));
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(mIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
