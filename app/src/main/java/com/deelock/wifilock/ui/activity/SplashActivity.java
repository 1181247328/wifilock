package com.deelock.wifilock.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.entity.AD;
import com.deelock.wifilock.entity.UserDetail;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.DensityUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by binChuan on 2017\10\9 0009.
 */

public class SplashActivity extends Activity {

    RelativeLayout bg;
    ImageView logoIv;
    TextView jump;

    private final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 170;

    private String path;

    private volatile boolean isJump = true;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bindActivity();
        setEvent();
    }

    protected void bindActivity() {
        setContentView(R.layout.activity_splash);
        path = getFilesDir() + "/local_cache";
        bg = findViewById(R.id.bg);
        logoIv = findViewById(R.id.logoIv);
        jump = findViewById(R.id.jump);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            } else {
                request();
            }
        } else {
            request();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void request() {
        SimpleDateFormat sf = new SimpleDateFormat("dd");
        String date = sf.format(System.currentTimeMillis());

        File file = new File(path, "ad.jpg");
        if (file.exists()) {
            if (SPUtil.getAdCheck(this, date)) {
                loadAd(file);
                requestAdUri();
                return;
            }
        }
        logoIv.setVisibility(View.VISIBLE);
        prepareJump();
        requestAdUri();
    }

    private void loadAd(File file) {
        Glide.with(this).load(file).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                bg.setBackground(glideDrawable);
                if (TextUtils.isEmpty(SPUtil.getToken(SplashActivity.this))) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isJump) {
                                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(mainIntent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        }
                    }, 5000);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isJump) {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }, 5000);
                    getData();
                }

                jump.setVisibility(View.VISIBLE);
                final int count = 4;
                Observable.interval(1, 1, TimeUnit.SECONDS)
                        .take(count + 1)
                        .map(new Function<Long, Long>() {
                            @Override
                            public Long apply(@NonNull Long aLong) throws Exception {
                                return count - aLong;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {

                            }
                        })
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull Long aLong) {
                                jump.setText("跳过\n" + aLong + "s");
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(SPUtil.getAdUri(SplashActivity.this));
                        Intent intent = new Intent(SplashActivity.this, ADActivity.class);
                        intent.setData(uri);
                        startActivityForResult(intent, 0);
                        handler.removeCallbacksAndMessages(null);
                    }
                });
            }
        });
    }

    private void requestAdUri() {
        Map params = new HashMap();
        params.put("height", DensityUtil.getScreenHeight(this));
        params.put("width", DensityUtil.getScreenWidth(this));
        params.put("versionCount", 100);
        params.put("type", 50);
        RequestUtils.requestUnLogged(RequestUtils.AD, getApplicationContext(), params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, final String content) {
                        super.onSuccess(code, content);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final AD ad = new Gson().fromJson(content, AD.class);
                                Bitmap bitmap = null;
                                try {
                                    bitmap = Glide.with(getApplicationContext())
                                            .load(ad.getUrl())
                                            .asBitmap()
                                            .skipMemoryCache(true)
                                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                                File file = new File(path, "ad.jpg");

                                File parentFile = file.getParentFile();
                                if (!parentFile.exists()) {// 如果文件夹不存在, 创建文件夹
                                    parentFile.mkdirs();
                                }

                                if (file.exists()) {
                                    file.delete();
                                }
                                // 将图片保存在本地
                                if (bitmap != null) {
                                    try {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                }
                                SPUtil.setAdUri(SplashActivity.this, ad.getMessage());
                            }
                        }).start();
                    }
                }
        );
    }

    private void getData() {
        Map params1 = new HashMap();
        params1.put("timestamp", TimeUtil.getTime());
        params1.put("pid", SPUtil.getUid(this));
        params1.put("headUrl", SPUtil.getUid(this));
        RequestUtils.request(RequestUtils.DETAIL_INFO, this, params1).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        UserDetail detail = new Gson().fromJson(content, UserDetail.class);
                        SPUtil.setHeadUrl(SplashActivity.this, detail.getHeadUrl());
                        Constants.headUrl = detail.getHeadUrl();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions,
                                           @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    request();
                } else {
                    Toast.makeText(getApplicationContext(), "缺少必要权限，请在权限管理中心打开", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isJump = false;
        if (TextUtils.isEmpty(SPUtil.getToken(SplashActivity.this))) {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void prepareJump() {
        if (TextUtils.isEmpty(SPUtil.getToken(SplashActivity.this))) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(mainIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }, 1500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);
            getData();
        }
    }

    protected void setEvent() {
        jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isJump = false;
                if (TextUtils.isEmpty(SPUtil.getToken(SplashActivity.this))) {
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(mainIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}