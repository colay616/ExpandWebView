package com.ljy.expandwebview;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 描   述:RealDownLoader
 * 作   者:lijiayan_mail@163.com
 * 创建日期:2017/7/14 10:35
 * 修改历史:
 */
public class RealDownLoader extends AsyncTask<Void, Integer, Integer> {

    private DownLoadTask mDownLoadTask;
    private long loaded = 0;
    private long totals = -1;
    private long tmp = 0;
    private long begin = 0;
    private long used = 1;
    private long mTimeLast = 0;
    private long mSpeed = 0;

    private static final int TIME_OUT = 30000000;
    private Notity mNotity;

    private static final int ERROR_LOAD = -5;

    RealDownLoader(DownLoadTask downLoadTask) {


        this.mDownLoadTask = downLoadTask;
        this.totals = mDownLoadTask.getLength();
        checkNullTask(downLoadTask);
    }

    private void checkNullTask(DownLoadTask downLoadTask) {

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        buildNotify(new Intent(), mDownLoadTask.getId(), "正在下载中");
    }

    private boolean checkDownLoaderCondition() {

        if (!checknet())
            return false;

        if (mDownLoadTask.getLength() - mDownLoadTask.getFile().length() > WebUtils.getAvailableStorage()) {
            return false;
        }

        return true;
    }

    private boolean checknet() {
        if (!mDownLoadTask.isForce()) {

            return WebUtils.checkWifi(mDownLoadTask.getContext());
        } else {
            return WebUtils.checkNetwork(mDownLoadTask.getContext());
        }


    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result;
        try {
            begin = System.currentTimeMillis();
            if (!checkDownLoaderCondition())
                return ERROR_LOAD;
            result = doDownLoad();
        } catch (Exception e) {

            e.printStackTrace();
            return ERROR_LOAD;
        }

        return result;
    }

    private int doDownLoad() throws IOException {

        HttpURLConnection mHttpURLConnection = createUrlConnection(mDownLoadTask.getUrl());


        if (mDownLoadTask.getFile().length() > 0) {

            mHttpURLConnection.addRequestProperty("Range", "bytes=" + (tmp = mDownLoadTask.getFile().length()) + "-");
        }

        mHttpURLConnection.connect();
        if (mHttpURLConnection.getResponseCode() != 200 && mHttpURLConnection.getResponseCode() != 206) {

            return ERROR_LOAD;
        }

        try {

            return doDownLoad(mHttpURLConnection.getInputStream(), new LoadingRandomAccessFile(mDownLoadTask.getFile()));
        } finally {
            if (mHttpURLConnection != null)
                mHttpURLConnection.disconnect();
        }


    }

    private HttpURLConnection createUrlConnection(String url) throws IOException {

        HttpURLConnection mHttpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        mHttpURLConnection.setRequestProperty("Accept", "application/*");
        mHttpURLConnection.setConnectTimeout(5000);
        return mHttpURLConnection;
    }

    private long time = 0;

    @Override
    protected void onProgressUpdate(Integer... values) {


        long current = System.currentTimeMillis();
        used = current - begin;
        long c = System.currentTimeMillis();
        if (mNotity != null && c - time > 100) {
            time = c;
            int currentprogress = (int) ((tmp + loaded) / Float.valueOf(totals) * 100);
            mNotity.setProgress(100, currentprogress, false);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {

        if (integer == ERROR_LOAD) {

            Toast.makeText(mDownLoadTask.getContext(), "下载失败", Toast.LENGTH_SHORT).show();
            if (mNotity != null)
                mNotity.cancel(mDownLoadTask.getId());
            return;
        }

        if (mDownLoadTask.isEnableIndicator()) {

            if (mNotity != null)
                mNotity.cancel(mDownLoadTask.getId());

            Intent notificationClickIntent = new Intent(mDownLoadTask.getContext(), OpenFileReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putString("fileAdsPath", mDownLoadTask.getFile().getAbsolutePath());
            notificationClickIntent.putExtras(bundle);
            notificationClickIntent.setAction("com.ljy.expandwebview.OpenFileReceiver");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mDownLoadTask.getContext(), 0, notificationClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotity.setProgressFinish("点击打开", pendingIntent);
        }

    }

    private void buildNotify(Intent intent, int id, String progressHint) {

        if (mDownLoadTask.isEnableIndicator()) {

            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent rightPendIntent = PendingIntent.getActivity(mDownLoadTask.getContext(),
                    0x33, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            int smallIcon = mDownLoadTask.getDrawableRes();
            String ticker = "您有一条新通知";
            mNotity = new Notity(mDownLoadTask.getContext(), id);
            mNotity.notify_progress(rightPendIntent, smallIcon, ticker, "文件下载", progressHint, false, false, false);
            mNotity.sent();
        }
    }

    private int doDownLoad(InputStream in, RandomAccessFile out) throws IOException {

        byte[] buffer = new byte[102400];
        BufferedInputStream bis = new BufferedInputStream(in, 102400);
        try {

            out.seek(out.length());

            int bytes = 0;
            long previousBlockTime = -1;

            while (!isCancelled()) {
                int n = bis.read(buffer, 0, 102400);
                if (n == -1) {
                    break;
                }
                out.write(buffer, 0, n);
                bytes += n;

                if (!checknet()) {
                    Log.i("Info", "network");
                    return ERROR_LOAD;
                }

                if (mSpeed != 0) {
                    previousBlockTime = -1;
                } else if (previousBlockTime == -1) {
                    previousBlockTime = System.currentTimeMillis();
                } else if ((System.currentTimeMillis() - previousBlockTime) > TIME_OUT) {
                    Log.i("Info", "timeout");
                    return ERROR_LOAD;
                }
            }
            return bytes;
        } finally {
            out.close();
            bis.close();
            in.close();
        }
    }

    private final class LoadingRandomAccessFile extends RandomAccessFile {

        public LoadingRandomAccessFile(File file) throws FileNotFoundException {
            super(file, "rw");
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            super.write(buffer, offset, count);
            loaded += count;
            publishProgress(0);

        }
    }
}
