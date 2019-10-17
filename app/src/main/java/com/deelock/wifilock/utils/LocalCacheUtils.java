package com.deelock.wifilock.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.deelock.wifilock.R;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.overwrite.CircleImageView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class LocalCacheUtils {

    public static final String CACHE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/local_cache";

    /**
     * 从本地sdcard读图片
     */
    public static Bitmap getBitmapFromLocal(String url) {
        try {
            File file = new File(CACHE_PATH, url);

            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setHeadInAnotherThread(final Context context, final String url, final CircleImageView view){
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
                Bitmap b = LocalCacheUtils.getBitmapFromLocal(url);
                if (b != null) {
                    e.onNext(LocalCacheUtils.getBitmapFromLocal(url));
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.history_head);
                    e.onNext(bitmap);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        Observable.create(new ObservableOnSubscribe<Integer>() {
//            @Override
//            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
//
//            }
//        }).subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Exception {
//
//            }
//        });
    }

    public static void setLocalHead(final Context context, final String url, final CircleImageView view){
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
                Bitmap b = LocalCacheUtils.getBitmapFromLocal(url);
                if (b != null) {
                    e.onNext(LocalCacheUtils.getBitmapFromLocal(url));
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 向sdcard写图片
     *
     * @param url1
     * @param bitmap
     */
    public static void setBitmapToLocal(String url1, Bitmap bitmap, Context context, String address, String old) {
        try {

            File file = new File(CACHE_PATH, url1);

            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {// 如果文件夹不存在, 创建文件夹
                parentFile.mkdirs();
            }

            // 将图片保存在本地
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            String boundary = "----boundary";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data2 = baos.toByteArray();

            StringBuilder sb = new StringBuilder();
            
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=uid");
            sb.append("\r\n\r\n");
            sb.append(SPUtil.getUid(context));
            sb.append("\r\n");

            if (old != null){
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=oldUrl");
                sb.append("\r\n\r\n");
                sb.append(old);
                sb.append("\r\n");
            }

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=token");
            sb.append("\r\n\r\n");
            sb.append(SPUtil.getToken(context));
            sb.append("\r\n");

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=timestamp");
            sb.append("\r\n\r\n");
            sb.append(TimeUtil.getTime());
            sb.append("\r\n");

            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=file; filename=file.jpg");
            sb.append("\r\n\r\n");

            String end = "\r\n--"+boundary+"--\r\n";

            byte[] data = sb.toString().getBytes();

            String result = "";
            BufferedReader reader = null;
            try {
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(sb.toString());
                out.write(data2);
                out.writeBytes(end);
                out.flush();
                if (conn.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    result = reader.readLine();
                    BaseResponse response = new Gson().fromJson(result, BaseResponse.class);
                    if (response.code == 1){
                        String content = response.content.toString();
                        if (content.indexOf("imgUrl") != -1){
                            String u = content.substring(8, content.length() - 1);
                            updateUrl(context, u);
                        }
                    }
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateUrl(final Context context, String url){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(context));
        params.put("headUrl", url);
        RequestUtils.request(RequestUtils.UPDATE_INFO, context, params).enqueue(
                new ResponseCallback<BaseResponse>((Activity) context) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(context, "修改成功");

                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                    }
                }
        );
    }

    private static RequestBody toRequestBody(String value){
        RequestBody body = RequestBody.create(MediaType.parse("form-data"), value);
        return body;
    }
}
