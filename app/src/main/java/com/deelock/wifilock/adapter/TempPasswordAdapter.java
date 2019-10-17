package com.deelock.wifilock.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemTempPasswordBinding;
import com.deelock.wifilock.entity.TempPassword;
import com.deelock.wifilock.network.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by binChuan on 2017\9\20 0020.
 */

public class TempPasswordAdapter extends RecyclerView.Adapter<TempPasswordAdapter.ViewHolder> {

    private List<TempPassword> data;
    private int layoutId;
    private SimpleDateFormat sf;

    public TempPasswordAdapter(List<TempPassword> data) {
        this.data = data;
        layoutId = R.layout.item_temp_password;
        sf = new SimpleDateFormat("yyyy/MM/dd HH:00");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTempPasswordBinding binding = DataBindingUtil.inflate(inflater, layoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.getBinding().nameTv.setText(data.get(position).getOpenName());
        if (data.get(position).getType() == 0){
            holder.getBinding().typeIv.setImageResource(R.mipmap.password_once);
        } else {
            holder.getBinding().typeIv.setImageResource(R.mipmap.time_password);
        }

        switch (data.get(position).getState()){
            case -3:{
                holder.getBinding().stateTv.setText("添加失败\n请开启远程下发功能");
                break;
            }
            case -2:{
                holder.getBinding().stateTv.setText("删除失败");
                break;
            }
            case -1:{
                holder.getBinding().stateTv.setText("添加失败");
                break;
            }
            case 0:{
                holder.getBinding().stateTv.setText("删除中...");
                break;
            }
            case 1:{
                if (data.get(position).getTimeEnd() < TimeUtil.getTime()){
                    holder.getBinding().stateTv.setText("已失效");
                } else {
                    holder.getBinding().stateTv.setText("正在使用");
                }
                break;
            }
            case 2:{
                holder.getBinding().stateTv.setText("添加中...");
                break;
            }
            case 3:{
                holder.getBinding().stateTv.setText("已失效");
                break;
            }
        }

        String s = sf.format(data.get(position).getTimeBegin() * 1000l) + " ~ " +
                sf.format(data.get(position).getTimeEnd() * 1000l);
        holder.getBinding().timeTv.setText(s);
        holder.getBinding().deleteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event == null){
                    return;
                }
                event.delete(position);
            }
        });

        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTempPasswordBinding binding;

        public ViewHolder(ItemTempPasswordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemTempPasswordBinding getBinding(){
            return binding;
        }
    }

    public interface Event{
        void delete(int p);
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
