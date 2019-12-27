package com.deelock.wifilock.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.deelock.wifilock.BuildConfig;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.utils.BluetoothUtil;
import com.mob.MobSDK;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.wanjian.cockroach.Cockroach;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by binChuan on 2017\9\13 0013.
 */

public class CustomApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
            MobSDK.init(this, "22242da69e853", "faa549bb71c29146a4ceec2f0340ed23");
            JPushInterface.init(this);
        } else {
            InitializeService.start(this);
        }

        Cockroach.install(new Cockroach.ExceptionHandler() {
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("CustomApplication", "--->CockroachException:" + thread + "<---", throwable);
//                            Toast.makeText(App.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Constants.start = System.currentTimeMillis();
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        BluetoothUtil.disConnect();
    }
}
