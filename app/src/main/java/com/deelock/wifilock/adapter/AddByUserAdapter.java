package com.deelock.wifilock.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ItemAddBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.utils.SPUtil;

import java.util.List;

/**
 * Created by binChuan on 2017\10\18 0018.
 */

public class AddByUserAdapter extends RecyclerView.Adapter<AddByUserAdapter.ViewHolder> {

    Context context;
    int layoutId;
    List<User> data;
    int type;
    String sdlId;
    String path;

    public AddByUserAdapter(Context context, List<User> data, int type, String sdlId) {
        this.context = context;
        this.data = data;
        layoutId = R.layout.item_add;
        this.type = type;
        this.sdlId = sdlId;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/local_cache";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAddBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        ViewHolder holder = new ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.getBinding().setName(data.get(position).getName());
        holder.getBinding().setType(data.get(position).getType());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null) {
                    event.add(position);
                }
            }
        });

        String authId = data.get(position).getPid();
        if (authId != null && authId.equals("11111111111111111111111111111111")) {
            String url = Constants.headUrl;
            if (url == null) {
                url = SPUtil.getHeadUrl(context);
            }
            Glide.with(context).load(url).error(R.mipmap.mine_head).into(holder.getBinding().headCiv);
        } else {
            Glide.with(context)
                    .load(data.get(position).getHeadUrl())
                    .error(R.mipmap.history_head)
                    .into(holder.getBinding().headCiv);
        }

        if (position == getItemCount() - 1) {
            holder.getBinding().dividerV.setVisibility(View.GONE);
        } else {
            holder.getBinding().dividerV.setVisibility(View.VISIBLE);
        }

        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemAddBinding binding;

        public ViewHolder(ItemAddBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemAddBinding getBinding() {
            return binding;
        }
    }

    public interface Event {
        void add(int p);
    }

    Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}