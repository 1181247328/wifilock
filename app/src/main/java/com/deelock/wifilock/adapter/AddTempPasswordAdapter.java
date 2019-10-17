package com.deelock.wifilock.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemAddBinding;
import com.deelock.wifilock.entity.TempPassword;
import com.deelock.wifilock.ui.activity.PasswordStepActivity;

import java.util.List;

/**
 * Created by binChuan on 2017\9\27 0027.
 */

public class AddTempPasswordAdapter extends RecyclerView.Adapter<AddTempPasswordAdapter.ViewHolder> {

    private Context context;
    List<TempPassword> data;
    private int layoutId;

    public AddTempPasswordAdapter(Context context, List<TempPassword> data){
        this.context = context;
        this.data = data;
        layoutId = R.layout.item_add;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAddBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        ViewHolder holder = new ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PasswordStepActivity.class);
                intent.putExtra("flag", 3);
                context.startActivity(intent);
            }
        });
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemAddBinding binding;

        public ViewHolder(ItemAddBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemAddBinding getBinding() {
            return binding;
        }
    }
}
