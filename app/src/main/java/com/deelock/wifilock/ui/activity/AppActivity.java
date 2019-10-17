package com.deelock.wifilock.ui.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public abstract class AppActivity extends AppCompatActivity {

    boolean clickAble = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        bindActivity();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        findView();
        doBusiness();
        requestData();
        setEvent();
    }

    protected <T extends View> T f(int viewId) {
        return (T) findViewById(viewId);
    }

    /**
     * 设置绑定页面
     */
    protected abstract void bindActivity();

    protected void findView(){}

    /**
     * 后续处理
     */
    protected abstract void doBusiness();

    /**
     * 获取数据
     */
    protected void requestData(){}

    /**
     * 设置监听
     */
    protected abstract void setEvent();

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        ToastUtil.toastShort(getApplicationContext(), "连接异常，请检查网络设置！");
        return false;
    }
}
