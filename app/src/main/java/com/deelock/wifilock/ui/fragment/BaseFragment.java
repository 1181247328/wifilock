package com.deelock.wifilock.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public abstract class BaseFragment extends Fragment {

    boolean clickAble = true;

    protected void openActivityForMoment(final Class<?> clz, final Bundle bundle){
        if (!clickAble){
            return;
        }
        clickAble = false;
        Intent intent = new Intent();
        intent.setClass(getContext(), clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        clickAble = true;
    }

    protected void openActivity(final Class<?> clz, final Bundle bundle){
        Intent intent = new Intent();
        intent.setClass(getContext(), clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
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
        ToastUtil.toastShort(getActivity().getApplicationContext(), "连接异常，请检查网络情况！");
        return false;
    }
}
