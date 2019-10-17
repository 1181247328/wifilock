package com.deelock.wifilock.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.EvaluateItem;

import java.util.List;

/**
 * Created by forgive for on 2018\2\2 0002.
 */

public class EvaluateAdapter extends RecyclerView.Adapter<EvaluateAdapter.ViewHolder> {

    private List<EvaluateItem> data;
    private int layoutId;

    public EvaluateAdapter(List<EvaluateItem> data){
        this.data = data;
        layoutId = R.layout.item_evaluate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = new ViewHolder(inflater.inflate(layoutId, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name_tv.setText(data.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name_tv;
        AppCompatRadioButton ensureRb;
        public ViewHolder(View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.name_tv);
            ensureRb = itemView.findViewById(R.id.ensureRb);
        }
    }

    public List<EvaluateItem> getData(){
        return data;
    }
}
