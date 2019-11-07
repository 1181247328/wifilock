package com.deelock.wifilock.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.EquipmentAdapter;
import com.deelock.wifilock.databinding.FragmentEquipmentBinding;
import com.deelock.wifilock.entity.DeviceStateList;
import com.deelock.wifilock.entity.GateWay;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.AddEquipmentActivity;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EquipmentFragment extends BaseFragment {

    FragmentEquipmentBinding binding;

    private List<Object> states;
    private EquipmentAdapter adapter;

    public EquipmentFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_equipment, container, false);
        doBusiness();
        return binding.getRoot();
    }

    private void doBusiness() {
        states = new ArrayList<>();
        if (states.size() == 0) {
            binding.bigAddIb.setVisibility(View.VISIBLE);
            binding.addNoticeTv.setVisibility(View.VISIBLE);
            binding.addIb.setVisibility(View.GONE);
        }
        adapter = new EquipmentAdapter(getActivity(), states);
        binding.rv.setAdapter(adapter);
        InitRecyclerView.initLinearLayoutVERTICAL(getActivity(), binding.rv);

        binding.addIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityForMoment(AddEquipmentActivity.class, null);
            }
        });

        binding.bigAddIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityForMoment(AddEquipmentActivity.class, null);
            }
        });

        binding.refreshSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.refreshSrl.setRefreshing(true);
                requestData();
            }
        });
    }

    private void requestData() {
        if (!isNetworkAvailable()) {
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(getContext()));
        RequestUtils.request(RequestUtils.LOCK_LIST, getContext(), params).enqueue(
                new ResponseCallback<BaseResponse>(getActivity()) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        states.clear();
                        Log.e("main", "---主页面---" + content);
                        DeviceStateList deviceStateList = new Gson().fromJson(content, DeviceStateList.class);
                        SPUtil.saveData(getContext(), SPUtil.LOCK_STATE, content);

                        List<LockState> lockList = deviceStateList.getSingleWifiObjs().getLockList();
                        if (lockList != null) {
                            for (LockState lockState : lockList) {
                                SPUtil.setSdlName(getContext(), lockState.getPid(), lockState.getNickName());
                                if (!TextUtils.isEmpty(lockState.getMacAddr())) {
                                    SPUtil.saveData(getContext(), lockState.getPid() + "mac", lockState.getMacAddr());
                                    if (lockState.getIsBtopen() == 1) {
                                        states.add(lockState);
                                    }
                                } else {
                                    states.add(lockState);
                                }
                            }
                        }
                        List<LockState> shareLockList = deviceStateList.getSingleWifiObjs().getShareLockList();
                        List<LockState> shareRealList = new ArrayList<>();
                        if (shareLockList != null) {

                            for (LockState lockState : shareLockList) {

                                if (lockState.getIsManager() == 1 || lockState.getIsManager() == 0) {    // 1.主用户  2.非主用户
                                    shareRealList.add(lockState);
                                } else {
                                    if (lockState.getIsBtopen() == 1) {  //被分享用户在isBtopen==1时代表可以使用
                                        shareRealList.add(lockState);
                                    }
                                }
//                                if (lockState.getIsBtopen() == 1) {
//                                    shareRealList.add(lockState);
//                                } else if (lockState.getIsBtopen() == 0 && lockState.getIsManager() == 0) {
//                                    lockList.add(lockState);     //暂时的
//                                }
                                SPUtil.setSdlName(getContext(), lockState.getPid(), lockState.getNickName());
                                if (!TextUtils.isEmpty(lockState.getMacAddr())) {
                                    SPUtil.saveData(getContext(), lockState.getPid() + "mac", lockState.getMacAddr());
                                }
                            }
                        }
                        List<LockState> magList = deviceStateList.getSingleWifiObjs().getMagnetometerList();
                        for (LockState magnetState : magList) {
                            SPUtil.setSdlName(getContext(), magnetState.getPid(), magnetState.getNickName());
                        }

                        for (GateWay gateWay : deviceStateList.getGatewayList()) {
                            SPUtil.setSdlName(getContext(), gateWay.getDevId(), gateWay.getDevName());
                        }
                        states.addAll(shareRealList);
                        states.addAll(deviceStateList.getSingleWifiObjs().getMagnetometerList());
                        states.addAll(deviceStateList.getGatewayList());
                        adapter.notifyDataSetChanged();
                        if (states.size() != 0) {
                            binding.bigAddIb.setVisibility(View.GONE);
                            binding.addNoticeTv.setVisibility(View.GONE);
                            binding.addIb.setVisibility(View.VISIBLE);
                            Intent intent = new Intent("com.deelock.wifilock.unSafe");
                            intent.putExtra("type", 2);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                        } else {
                            Intent intent = new Intent("com.deelock.wifilock.unSafe");
                            intent.putExtra("type", 3);
                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                            binding.bigAddIb.setVisibility(View.VISIBLE);
                            binding.addNoticeTv.setVisibility(View.VISIBLE);
                            binding.addIb.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(getContext(), message);
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        binding.refreshSrl.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        requestData();
    }
}
