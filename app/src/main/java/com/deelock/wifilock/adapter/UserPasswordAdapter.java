package com.deelock.wifilock.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemPasswordBinding;
import com.deelock.wifilock.databinding.ItemUserDetailBinding;
import com.deelock.wifilock.entity.UserPassword;

import java.io.File;
import java.util.List;

/**
 * Created by binChuan on 2017\9\21 0021.
 */

public class UserPasswordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserPassword> data;
    private int[] layoutId;

    private final int USER = 1;
    private final int PASSWORD = 2;
    private String sdlId;
    private Context context;
    String path;

    public UserPasswordAdapter(Context context, List<UserPassword> data, String sdlId) {
        this.data = data;
        this.layoutId = new int[2];
        layoutId[0] = R.layout.item_user_detail;
        layoutId[1] = R.layout.item_password;
        this.sdlId = sdlId;
        this.context = context;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/local_cache";
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == USER) {
            ItemUserDetailBinding binding1 = DataBindingUtil.inflate(inflater, layoutId[0], parent, false);
            return new UserViewHolder(binding1);
        } else {
            ItemPasswordBinding binding2 = DataBindingUtil.inflate(inflater, layoutId[1], parent, false);
            return new PasswordViewHolder(binding2);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).getBinding().nameTv.setText(data.get(position).getName());
            String url = "123";
            String authId = data.get(position).getAuthId();
            if (authId != null && !authId.equals("11111111111111111111111111111111")) {
                url = sdlId + data.get(position).getAuthId();
            }
            File file = new File(path, url);
            Glide.with(context)
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .error(R.mipmap.history_head)
                    .into(((UserViewHolder) holder).getBinding().headCiv);
            ((UserViewHolder) holder).getBinding().executePendingBindings();
        } else {
            ((PasswordViewHolder) holder).getBinding().nameTv.setText(data.get(position).getOpenName());

            if (position == data.size() - 1) {
                ((PasswordViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
            } else {
                ((PasswordViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
            }

            switch (data.get(position).getState()) {
                case -3: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("添加失败\n请开启远程下发功能");
                    break;
                }
                case -2: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("删除失败");
                    break;
                }
                case -1: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("添加失败");
                    break;
                }
                case 0: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("删除中...");
                    break;
                }
                case 1: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("正在使用");
                    break;
                }
                case 2: {
                    ((PasswordViewHolder) holder).getBinding().stateTv.setText("添加中...");
                    break;
                }
            }

            if (data.get(position).getType() == 1) {
                ((PasswordViewHolder) holder).getBinding().deleteIb.setVisibility(View.GONE);
                ((PasswordViewHolder) holder).getBinding().managerIv.setVisibility(View.VISIBLE);
            }

            ((PasswordViewHolder) holder).getBinding().deleteIb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event.deleteUser(position);
                }
            });

            ((PasswordViewHolder) holder).getBinding().executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getUser() == 1) {
            return USER;
        }
        return PASSWORD;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserDetailBinding binding;

        public UserViewHolder(ItemUserDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserDetailBinding getBinding() {
            return binding;
        }
    }

    class PasswordViewHolder extends RecyclerView.ViewHolder {
        private ItemPasswordBinding binding;

        public PasswordViewHolder(ItemPasswordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemPasswordBinding getBinding() {
            return binding;
        }
    }

    public interface Event {
        void deleteUser(int p);
    }

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
