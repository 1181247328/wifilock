package com.deelock.wifilock.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.FragmentMineBinding;
import com.deelock.wifilock.entity.UserDetail;
import com.deelock.wifilock.event.MineEvent;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.AboutActivity;
import com.deelock.wifilock.ui.activity.CenterActivity;
import com.deelock.wifilock.ui.activity.SplashActivity;
import com.deelock.wifilock.utils.LocalCacheUtils;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public class MineFragment extends BaseFragment {

    FragmentMineBinding binding;

    BroadcastReceiver receiver;
    LocalBroadcastManager broadcastManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mine, container, false);
        doBusiness();
        setEvent();
        return binding.getRoot();
    }

    private void doBusiness() {
        broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter("com.deelock.wifilock.name");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("name");
                binding.nameTv.setText(name);
            }
        };
        broadcastManager.registerReceiver(receiver, intentFilter);
        binding.nameTv.setText(SPUtil.getNickName(getContext()));
        String phoneNum = SPUtil.getPhoneNumber(getContext());
        binding.phoneNumberTv.setText(phoneNum.substring(0, 3) + "****" + phoneNum.substring(7));
    }

    private void setEvent() {
        binding.setEvent(new MineEvent() {
            @Override
            public void center() {
                openActivityForMoment(CenterActivity.class, null);
            }

            @Override
            public void about() {
                openActivityForMoment(AboutActivity.class, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHead();
    }

    private void loadHead(){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(getContext()));
        params.put("headUrl", SPUtil.getUid(getContext()));
        RequestUtils.request(RequestUtils.DETAIL_INFO, getContext(), params).enqueue(
                new ResponseCallback<BaseResponse>(getActivity()) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        UserDetail detail = new Gson().fromJson(content, UserDetail.class);
                        Glide.with(getContext()).load(detail.getHeadUrl()).error(R.mipmap.user_head).into(binding.headCiv);
                    }
                }
        );
    }
}
