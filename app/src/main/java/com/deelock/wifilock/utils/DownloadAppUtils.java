package com.deelock.wifilock.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.deelock.wifilock.compment.UpdateAppReceiver;

import java.io.File;


/**
 * Created by Administrator on 2017\11\9 0009.
 */

public class DownloadAppUtils {

    private static final String TAG = DownloadAppUtils.class.getSimpleName();
    public static long downloadUpdateApkId = -1;//下载更新Apk 下载任务对应的Id
    public static String downloadUpdateApkFilePath;//下载更新Apk 文件路径

    /**
     * 通过浏览器下载APK包
     * @param context
     * @param url
     */
    public static void downloadForWebView(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        context.startActivity(Intent.createChooser(intent, "请选择浏览器"));
    }


    /**
     * 下载更新apk包
     * 权限:1,<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
     * @param context
     * @param url
     */
    public static void downloadForAutoInstall(Context context, String url, String fileName, String title) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!canDownloadState(context)){
            downloadForWebView(context, url);
            return;
        }
        try {
            Uri uri = Uri.parse(url);
            DownloadManager downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            //在通知栏中显示
            request.setVisibleInDownloadsUi(true);
            request.setTitle(title);
            String filePath = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//外部存储卡
                filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                Log.i(TAG,"没有SD卡");
                return;
            }
            downloadUpdateApkFilePath = filePath + File.separator + fileName;
            // 若存在，则删除
            deleteFile(downloadUpdateApkFilePath);
            Uri fileUri = Uri.fromFile(new File(downloadUpdateApkFilePath));
            request.setDestinationUri(fileUri);
            downloadUpdateApkId = downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            downloadForWebView(context, url);
        }finally {
            context.registerReceiver(new UpdateAppReceiver(), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    private static boolean canDownloadState(Context ctx) {
        try {
            int state = ctx.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
                if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                        || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                            return false;
                        }
        } catch (Exception e) {
            e.printStackTrace();
                return false;
        }
        return true;
    }

    private static boolean deleteFile(String fileStr) {
        File file = new File(fileStr);
        return file.delete();
    }
}
