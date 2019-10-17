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
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.ui.activity.FPrintStepActivity;

import java.util.List;

/**
 * Created by binChuan on 2017\9\25 0025.
 */

public class AddFPrintAdapter extends RecyclerView.Adapter<AddFPrintAdapter.ViewHolder> {

    private Context context;
    private int layoutId;
    private List<User> data;

    public AddFPrintAdapter(Context context, List<User> data){
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
                Intent intent = new Intent(context, FPrintStepActivity.class);
                intent.putExtra("type", 2);
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
        private ItemAddBinding binding;

        public ViewHolder(ItemAddBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemAddBinding getBinding() {
            return binding;
        }
    }
}
