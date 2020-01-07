package com.deelock.wifilock.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.state.LockMac;
import com.deelock.wifilock.R;
import com.deelock.wifilock.bluetooth.BleActivity;
import com.deelock.wifilock.bluetooth.BleAddPwdActivity;
import com.deelock.wifilock.databinding.ItemEquipmentBinding;
import com.deelock.wifilock.entity.GateWay;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.entity.PasswordList;
import com.deelock.wifilock.entity.TempPassword;
import com.deelock.wifilock.entity.UserPassword;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.EquipmentActivity;
import com.deelock.wifilock.ui.activity.GateWayActivity;
import com.deelock.wifilock.ui.activity.MagnetometerActivity;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;

import java.util.HashMap;
import java.util.List;

public class EquipmentAdapter extends CommonAdapter<EquipmentAdapter.ViewHolder> {

    private List<Object> data;
    private int layoutId;
    private int LOCK = 10;   //门锁
    private int MAGNETIC = 20;   //wifi门磁
    private int GATE = 30;    //网关
    private int BLUETOOTH = 40;    //蓝牙

    public EquipmentAdapter(Context context, List<Object> data) {
        this.context = context;

        this.data = data;
        layoutId = R.layout.item_equipment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEquipmentBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        switch (type) {
            case 40:
            case 10:
                LockState lock = (LockState) data.get(position);
                boolean isOnline = (lock.getIsOnline() == 0);
                String nickName = lock.getNickName();
                if (!TextUtils.isEmpty(nickName)) {
                    holder.getBinding().nameTv.setText(nickName);
                } else {
                    holder.getBinding().nameTv.setText("智能门锁");
                }
                if (type == 40 && TextUtils.isEmpty(lock.getSsid())) {
                    holder.getBinding().onlineTv.setVisibility(View.INVISIBLE);
                } else {
                    holder.getBinding().onlineTv.setVisibility(View.VISIBLE);
                    holder.getBinding().onlineTv.setText(isOnline ? "离线" : "在线");
                    holder.getBinding().onlineTv.setTextColor(isOnline ? 0xfff54528 : 0xff13c6e5);
                }
                if (type == 40 && lock.getIsBtopen() == 1 && lock.getIsManager() == 2) {
                    holder.getBinding().onlineTv.setVisibility(View.VISIBLE);
                    holder.getBinding().onlineTv.setText("被授权");
                }
                holder.getBinding().iconIv.setImageResource(R.mipmap.lock);
                break;
            case 20:
                LockState lock_mag = (LockState) data.get(position);
                boolean isOnline1 = (lock_mag.getIsOnline() == 0);
                String magNickName = lock_mag.getNickName();
                if ("B002".equals(lock_mag.getPid().substring(0, 4))) {
                    holder.getBinding().nameTv.setText(("智能门磁".equals(magNickName) || TextUtils.isEmpty(magNickName)) ? "人体红外" : magNickName);
                } else {
                    if (!TextUtils.isEmpty(magNickName)) {
                        holder.getBinding().nameTv.setText(magNickName);
                    } else {
                        holder.getBinding().nameTv.setText("智能门磁");
                    }
                }
                holder.getBinding().onlineTv.setText(isOnline1 ? "离线" : "在线");
                holder.getBinding().onlineTv.setTextColor(isOnline1 ? 0xfff54528 : 0xff13c6e5);
                holder.getBinding().iconIv.setImageResource(R.mipmap.magnetic);
                break;
            case 30:
                GateWay gateWay = (GateWay) data.get(position);
                boolean isOnline2 = gateWay.getIsOnline() == 0;
                String devName = gateWay.getDevName();
                if (!TextUtils.isEmpty(devName)) {
                    holder.getBinding().nameTv.setText(devName);
                } else {
                    holder.getBinding().nameTv.setText("网关");
                }
                holder.getBinding().onlineTv.setText(isOnline2 ? "离线" : "在线");
                holder.getBinding().onlineTv.setTextColor(isOnline2 ? 0xfff54528 : 0xff13c6e5);
                holder.getBinding().iconIv.setImageResource(R.mipmap.gateway);
                break;
        }

        if (position == data.size() - 1) {
            holder.getBinding().dividerV.setVisibility(View.GONE);
        }

        final int finalType = type;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                switch (finalType) {
                    case 10:
                        //wifi门锁
                        b.putParcelable("lockState", (LockState) data.get(position));
                        openActivityForMoment(EquipmentActivity.class, b);
                        break;
                    case 20:
                        //门磁
                        b.putString("DeviceId", ((LockState) data.get(position)).getPid());
                        openActivityForMoment(MagnetometerActivity.class, b);
                        break;
                    case 30:
                        //网关
                        b.putParcelable("gateState", (GateWay) data.get(position));
                        openActivityForMoment(GateWayActivity.class, b);
                        break;
                    case 40:
                        //蓝牙锁
                        requestPw(position);
                }
            }
        });
        holder.getBinding().executePendingBindings();
    }

    /**
     * 请求所有密码
     */
    private void requestPw(final int position) {
        HashMap<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(TimeUtil.getTime()));
        params.put("uid", SPUtil.getUid(context));
        params.put("sdlId", ((LockState) data.get(position)).getPid());
        RequestUtils.request(RequestUtils.ALL_PASSWORD, context, params).enqueue(
                new ResponseCallback<BaseResponse>((Activity) context) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        PasswordList passwordList = GsonUtil.json2Bean(content, PasswordList.class);
                        List<TempPassword> tempPasswords = passwordList.getTempPasswords();
                        List<UserPassword> userPasswords = passwordList.getUserPasswords();
                        int pwSize = tempPasswords.size() + userPasswords.size();
                        if (pwSize == 0) {
                            Intent intent = new Intent(context, BleAddPwdActivity.class);
                            intent.putExtra("isFromBind", true);
                            intent.putExtra("deviceId", ((LockState) data.get(position)).getPid());
                            intent.putExtra("mac", ((LockState) data.get(position)).getMacAddr());
                            context.startActivity(intent);
                            return;
                        }
                        Bundle b = new Bundle();
                        b.putParcelable("lockState", (LockState) data.get(position));
                        //保存好MAC地址和版本号
                        LockMac lockMac = new LockMac();
                        //保存mac地址
                        lockMac.setMac(((LockState) data.get(position)).getMacAddr());
                        com.deelock.state.LockState.getLockState().setLockMac(lockMac);
                        openActivityForMoment(BleActivity.class, b);
                    }
                }
        );
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof LockState) {
            LockState lockState = (LockState) data.get(position);
            if (lockState.getPid().substring(0, 3).equals("A00")) {
                if (lockState.getPid().substring(0, 4).equals("A003")) {
                    return BLUETOOTH;
                } else {
                    return LOCK;
                }
            } else {
                return MAGNETIC;
            }
        } else {
            return GATE;
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemEquipmentBinding binding;

        public ViewHolder(ItemEquipmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemEquipmentBinding getBinding() {
            return binding;
        }
    }
}
