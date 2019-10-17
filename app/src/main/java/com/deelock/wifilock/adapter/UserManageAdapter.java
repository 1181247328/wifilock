package com.deelock.wifilock.adapter;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemUserManageBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserDetail;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.activity.UserActivity;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\22 0022.
 */

public class UserManageAdapter extends CommonAdapter<UserManageAdapter.ViewHolder> {

    private List<User> data;
    private int layoutId;
    private String sdlId;
    String path;

    public UserManageAdapter(Context context, List<User> data, String sdlId) {
        this.context = context;
        this.data = data;
        layoutId = R.layout.item_user_manage;
        this.sdlId = sdlId;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/local_cache";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUserManageBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.getBinding().nameTv.setText(data.get(position).getName());
        if (position == data.size() - 1) {
            holder.getBinding().dividerV.setVisibility(View.GONE);
        } else {
            holder.getBinding().dividerV.setVisibility(View.VISIBLE);
        }

        String headUrl = data.get(position).getHeadUrl();
        if (data.get(position).getPid().equals("11111111111111111111111111111111")) {
            Map params = new HashMap();
            params.put("timestamp", TimeUtil.getTime());
            params.put("pid", SPUtil.getUid(context));
            params.put("headUrl", SPUtil.getUid(context));
            RequestUtils.request(RequestUtils.DETAIL_INFO, context, params).enqueue(
                    new ResponseCallback<BaseResponse>((Activity) context) {
                        @Override
                        protected void onSuccess(int code, String content) {
                            super.onSuccess(code, content);
                            UserDetail detail = new Gson().fromJson(content, UserDetail.class);
                            Glide.with(context).load(detail.getHeadUrl()).error(R.mipmap.user_head).into(holder.getBinding().headCiv);
                        }
                    }
            );
        } else {
            Glide.with(context).load(headUrl).error(R.mipmap.mine_head).into(holder.getBinding().headCiv);
        }

        if (data.get(position).getType() == 0) {
            holder.getBinding().managerIv.setVisibility(View.GONE);
        } else {
            holder.getBinding().managerIv.setVisibility(View.VISIBLE);
        }

        if (data.get(position).getPid().equals("00000000000000000000000000000000")) {
            holder.getBinding().userTypeTv.setVisibility(View.VISIBLE);
            holder.getBinding().userTypeTv.setText("本地");
        } else if (data.get(position).getPid().equals("11111111111111111111111111111111")) {
            holder.getBinding().userTypeTv.setVisibility(View.VISIBLE);
            holder.getBinding().userTypeTv.setText("主用户");
        } else {
            holder.getBinding().userTypeTv.setVisibility(View.GONE);
        }

        if (data.get(position).getUserRight() == 0) {
            holder.getBinding().typeTv.setVisibility(View.VISIBLE);
            holder.getBinding().typeTv.setText("密码");
        } else if (data.get(position).getUserRight() == 1) {
            holder.getBinding().typeTv.setVisibility(View.VISIBLE);
            holder.getBinding().typeTv.setText("指纹");
        } else if (data.get(position).getUserRight() == 2) {
            holder.getBinding().typeTv.setVisibility(View.VISIBLE);
            holder.getBinding().typeTv.setText("指纹 | 密码");
        } else {
            holder.getBinding().typeTv.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("authId", data.get(position).getPid());
                b.putString("sdlId", sdlId);
                b.putInt("type", data.get(position).getType());
                b.putInt("userRight", data.get(position).getUserRight());
                b.putString("name", data.get(position).getName());
                b.putInt("isPush", data.get(position).getIsPush());
                b.putString("headUrl", data.get(position).getHeadUrl());
                b.putInt("isBtopen", data.get(position).getIsBtopen());
                b.putString("phoneNumber", data.get(position).getPhoneNumber());
                b.putInt("authUid", data.get(position).getAuthUid());
                b.putParcelableArrayList("userList", (ArrayList<? extends Parcelable>) data);
                openActivityForMoment(UserActivity.class, b);
            }
        });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemUserManageBinding binding;

        public ViewHolder(ItemUserManageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserManageBinding getBinding() {
            return binding;
        }
    }
}
