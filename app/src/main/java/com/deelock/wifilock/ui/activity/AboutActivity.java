package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ActivityAboutBinding;
import com.deelock.wifilock.entity.Version;
import com.deelock.wifilock.event.AboutEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.PhoneDialog;
import com.deelock.wifilock.utils.DownloadAppUtils;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class AboutActivity extends BaseActivity {

    ActivityAboutBinding binding;

    Version version;
    double apkSize;
    String apkUrl;
    int REQUESTPERMISSION = 110;
    AlertDialog dialog;
    WeakReference<Context> contextRef;


    private PhoneDialog phoneDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
//        presenter = new AboutPresenter(this, this);
//        presenter.getInfo();
        String versionName = "";
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        int year = c.get(Calendar.YEAR);
//        if (view != null){
//            view.setCopyRight();
//        }
        binding.copyrightTv.setText("成都迪洛可科技有限公司 版权所有\nCopyright © 2017 - " + year + " Chengdu Deelock Technology Co., Ltd. \nAll rights reserved.");

        try {
            PackageInfo packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            Log.d("TAG", "本软件的版本号。。" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        binding.versionTv.setText("v" + versionName);
    }

    @Override
    protected void doBusiness() {
        contextRef = new WeakReference<Context>(this);

        phoneDialog = new PhoneDialog(this, R.style.dialog);
        phoneDialog.setPhoneClick(new PhoneDialog.PhoneClick() {
            @Override
            public void callPhone() {
                phoneDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_CALL);
                String format_phone = getString(R.string.phone_deelock);
                String replace = format_phone.replace("-", "");
                Uri data = Uri.parse("tel:" + replace);
                intent.setData(data);
                if (ActivityCompat.checkSelfPermission(AboutActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AboutActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE}, 400);
                } else {
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 获取versionCode
     */
    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.setEvent(new AboutEvent() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void function() {

            }

            @Override
            public void update() {
                if (!isNetworkAvailable()) {
                    return;
                }

                //更新版本
                final int versionCode = getLockVersionCode();
                Map params = new HashMap();
                params.put("token", SPUtil.getToken(AboutActivity.this));
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(AboutActivity.this));
                params.put("type", 10);
                params.put("versionCount", versionCode);
                RequestUtils.request(RequestUtils.VERSION, AboutActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(AboutActivity.this) {
                            @SuppressLint("CheckResult")
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (content.length() < 5) {
                                    ToastUtil.toastLong(getApplicationContext(), "已是最新版本");
                                    return;
                                }
                                String r = content;
                                String u = null;
                                if (content.indexOf("http") != -1) {
                                    u = content.substring(content.indexOf("http"), content.length() - 2);
                                    apkUrl = u;
                                }
                                version = new Gson().fromJson(content, Version.class);
                                if (version == null) {
                                    return;
                                }
                                if (version.getVersionCount() > versionCode) {
                                    final String finalU = u;
                                    Observable.create(new ObservableOnSubscribe<Double>() {
                                        @Override
                                        public void subscribe(@NonNull ObservableEmitter<Double> e) throws Exception {
                                            HttpURLConnection conn = null;
                                            URL url = null;
                                            try {
                                                url = new URL(finalU);
                                                conn = (HttpURLConnection) url.openConnection();
                                                conn.setRequestProperty("Accept-Encoding", "identity");
                                                conn.setConnectTimeout(10000);
                                                conn.connect();
                                            } catch (MalformedURLException ee) {
                                                ee.printStackTrace();
                                            } catch (IOException eee) {
                                                eee.printStackTrace();
                                            }
                                            apkSize = conn.getContentLength() / 1048576.0;
                                            conn.disconnect();
                                            if (apkSize > 0) {
                                                e.onNext(apkSize);
                                            }
                                        }
                                    }).subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Consumer<Double>() {
                                                @Override
                                                public void accept(Double aDouble) throws Exception {
                                                    createDialog();
                                                }
                                            });
                                }
                            }
                        }
                );
            }

            @Override
            public void protocol() {
                startActivity(new Intent(AboutActivity.this, ProtocolActivity.class));
            }

            @Override
            public void call() {
                //拨打电话
                phoneDialog.show();
            }
        });
    }

    /**
     * 显示下载对话框
     */
    private void createDialog() {
        if (contextRef.get() == null) {
            return;
        }
        showDialog(0);
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field negativeButton = mAlertController.getClass().getDeclaredField("mButtonNegative");
            Field positionButton = mAlertController.getClass().getDeclaredField("mButtonPositive");
            negativeButton.setAccessible(true);
            positionButton.setAccessible(true);
            Button button1 = (Button) negativeButton.get(mAlertController);
            Button button2 = (Button) positionButton.get(mAlertController);
            button1.setTextColor(0xff717171);
            button2.setTextColor(0xff01bff2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求开启权限
     */
    private void askPermission() {
        if (ContextCompat.checkSelfPermission(AboutActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AboutActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTPERMISSION);
        } else {
            DownloadAppUtils.downloadForWebView(AboutActivity.this, apkUrl);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTPERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DownloadAppUtils.downloadForWebView(AboutActivity.this, apkUrl);
                } else {
//提示没有权限，安装不了咯
                }
            }
        } else if (requestCode == 400) {
            if (permissions[0].equals(Manifest.permission.CALL_PHONE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    String format_phone = getString(R.string.phone_deelock);
                    String replace = format_phone.replace("-", "");
                    Uri data = Uri.parse("tel:" + replace);
                    intent.setData(data);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "拨打电话需要开启权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getLocakVersionName() {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }

    private int getLockVersionCode() {
        // 获取packagemanager的实例
        PackageManager packageManager = getApplicationContext().getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int version = packInfo.versionCode;
        return version;
    }

    @Override
    protected AlertDialog onCreateDialog(int id) {
        DecimalFormat df = new DecimalFormat(".##");
        Double d = Double.valueOf(df.format(apkSize));
        String title = "新版本 V" + version.getVersion();
        String message = "安装包大小 " + d + "MB.\n" + version.getMessage().replace("；", "；\n");
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("立即下载",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                askPermission();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
        return dialog;
    }

}
