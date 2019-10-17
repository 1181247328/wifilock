package com.deelock.wifilock.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.deelock.wifilock.utils.KeyboardUtil;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\13 0013.
 */

public abstract class BaseActivity extends Activity {

    boolean clickAble = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (setFullScreen()){
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
        bindActivity();
        findView();
        doBusiness();
        requestData();
        setEvent();
        setCallBack();
    }

    protected <T extends View> T f(int viewId) {
        return findViewById(viewId);
    }

    protected boolean setFullScreen(){
        return false;
    }

    protected void findView(){

    }

    /**
     * 设置绑定页面
     */
    protected abstract void bindActivity();

    /**
     * 后续处理
     */
    protected abstract void doBusiness();

    /**
     * 请求数据
     */
    protected void requestData(){}

    /**
     * 设置回调
     */
    protected void setCallBack(){}

    /**
     * 设置监听
     */
    protected abstract void setEvent();

    protected void openActivityForMoment(final Class<?> clz, final Bundle bundle){
        if (!clickAble){
            return;
        }
        clickAble = false;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(Constants.START_ACTIVITY_DELAY);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Intent intent = new Intent();
//                intent.setClass(BaseActivity.this, clz);
//                if (bundle != null) {
//                    intent.putExtras(bundle);
//                }
//                startActivity(intent);
//                clickAble = true;
//            }
//        }).start();
        Intent intent = new Intent();
        intent.setClass(BaseActivity.this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        clickAble = true;
    }

    protected void openActivity(final Class<?> clz, final Bundle bundle){
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    protected void setNickNameInputFilter(EditText editText){
        editText.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) &&
                                    !Character.toString(source.charAt(i)).equals("_")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(16)
        });
    }

    protected void setNameInputFilter(EditText editText){
        editText.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) &&
                                    !Character.toString(source.charAt(i)).equals("_")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(4)
        });
    }

    protected void setWifiPasswordInputFilter(EditText editText){
        editText.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (Character.toString(source.charAt(i)).equals("\n")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(30)
        });
    }

    public boolean isNetworkAvailable() {
        try {
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
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    protected void hideInputKeyboard(){
        if (KeyboardUtil.isSoftInputShow(this)){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
//                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }
}
