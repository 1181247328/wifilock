package com.deelock.wifilock.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public class SimpleAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private List<T> data;

    private int layoutId;

    private int brId;

    public SimpleAdapter() {
    }

    public SimpleAdapter(List<T> data, int layoutId, int brId) {
        this.data = data;
        this.layoutId = layoutId;
        this.brId = brId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding.getRoot());
        viewHolder.setBinding(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.getBinding().setVariable(brId, data.get(position));
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }
}
