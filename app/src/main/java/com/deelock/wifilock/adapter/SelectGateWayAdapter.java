package com.deelock.wifilock.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.GateWay;
import com.deelock.wifilock.ui.activity.BindGateWayActivity;
import com.deelock.wifilock.ui.activity.BindLockActivity;
import com.deelock.wifilock.ui.activity.SelectGateActivity;

import java.util.List;

public class SelectGateWayAdapter extends RecyclerView.Adapter<SelectGateWayAdapter.SelectGateHolder> {

    private List<GateWay> list;
    private Context context;

    public SelectGateWayAdapter(List<GateWay> list) {
        this.list = list;
    }

    @Override
    public SelectGateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_select_gate, parent, false);
        return new SelectGateHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SelectGateHolder holder, final int position) {
        holder.name.setText(list.get(position).getDevName());
        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BindLockActivity.class);
                intent.putExtra("devId",list.get(position).getDevId());
                intent.putExtra("type", SelectGateActivity.type);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class SelectGateHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private RelativeLayout rl;

        public SelectGateHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.select_gate_name);
            rl = itemView.findViewById(R.id.select_gate_rl);
        }
    }
}
