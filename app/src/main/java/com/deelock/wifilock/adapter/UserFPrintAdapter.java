package com.deelock.wifilock.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
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
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.utils.LocalCacheUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by binChuan on 2017\9\21 0021.
 */

public class UserFPrintAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserFPrint> data;
    private int[] layoutId;

    private final int USER = 1;
    private final int PASSWORD = 2;
    String sdlId;
    Context context;
    String path;

    public UserFPrintAdapter(Context context, List<UserFPrint> data, String sdlId) {
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
        if (viewType == USER){
            ItemUserDetailBinding binding1 = DataBindingUtil.inflate(inflater, layoutId[0], parent, false);
            return new UserViewHolder(binding1);
        } else {
            ItemPasswordBinding binding2 = DataBindingUtil.inflate(inflater, layoutId[1], parent, false);
            return new FPrintViewHolder(binding2);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).getBinding().nameTv.setText(data.get(position).getName());

            String url = "123";
            String authId = data.get(position).getAuthId();
            if (authId != null && !authId.equals("11111111111111111111111111111111")){
                url = sdlId + authId;
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
            if (position + 1 >= getItemCount() || data.get(position+1).getUser() == 1){
                ((FPrintViewHolder) holder).getBinding().blockV.setVisibility(View.VISIBLE);
                ((FPrintViewHolder) holder).getBinding().fullDividerV.setVisibility(View.VISIBLE);
                ((FPrintViewHolder) holder).getBinding().dividerV.setVisibility(View.GONE);
            } else {
                ((FPrintViewHolder) holder).getBinding().blockV.setVisibility(View.GONE);
                ((FPrintViewHolder) holder).getBinding().fullDividerV.setVisibility(View.GONE);
                ((FPrintViewHolder) holder).getBinding().dividerV.setVisibility(View.VISIBLE);
            }
            ((FPrintViewHolder) holder).getBinding().nameTv.setText(data.get(position).getOpenName());
            if (data.get(position).getType() == 1){
                ((FPrintViewHolder) holder).getBinding().managerIv.setVisibility(View.VISIBLE);
            } else {
                ((FPrintViewHolder) holder).getBinding().managerIv.setVisibility(View.GONE);
            }
            if (data.get(position).getState() == 1){
                ((FPrintViewHolder) holder).getBinding().stateTv.setVisibility(View.GONE);
                ((FPrintViewHolder) holder).getBinding().deleteIb.setVisibility(View.VISIBLE);
            } else if (data.get(position).getState() == 0){
                ((FPrintViewHolder) holder).getBinding().stateTv.setVisibility(View.VISIBLE);
                ((FPrintViewHolder) holder).getBinding().stateTv.setText("删除中...");
                ((FPrintViewHolder) holder).getBinding().deleteIb.setVisibility(View.GONE);
            } else {
                ((FPrintViewHolder) holder).getBinding().stateTv.setText("删除失败");
                ((FPrintViewHolder) holder).getBinding().deleteIb.setVisibility(View.VISIBLE);
            }

            if (data.get(position).getPid().equals("0100")){
                ((FPrintViewHolder) holder).getBinding().deleteIb.setVisibility(View.GONE);
            } else {
                ((FPrintViewHolder) holder).getBinding().deleteIb.setVisibility(View.VISIBLE);
            }

            ((FPrintViewHolder) holder).getBinding().deleteIb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event != null){
                        event.delete(position);
                    }
                }
            });
            ((FPrintViewHolder) holder).getBinding().executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getUser() == 1){
            return USER;
        }
        return PASSWORD;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        private ItemUserDetailBinding binding;

        public UserViewHolder(ItemUserDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemUserDetailBinding getBinding(){
            return binding;
        }
    }

    class FPrintViewHolder extends RecyclerView.ViewHolder{
        private ItemPasswordBinding binding;

        public FPrintViewHolder(ItemPasswordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemPasswordBinding getBinding(){
            return binding;
        }
    }

    public interface Event{
        void delete(int position);
    }

    Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
