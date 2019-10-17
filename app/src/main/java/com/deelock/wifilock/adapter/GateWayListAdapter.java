package com.deelock.wifilock.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deelock.wifilock.R;
import com.deelock.wifilock.databinding.ItemGatewayBinding;
import com.deelock.wifilock.entity.GateDetail;
import com.deelock.wifilock.entity.LockState;
import com.deelock.wifilock.ui.activity.EquipmentActivity;
import com.deelock.wifilock.ui.activity.MagnetometerActivity;

import java.util.List;

public class GateWayListAdapter extends RecyclerView.Adapter<GateWayListAdapter.GateViewHolder> {

    private List<GateDetail.SubListBean> list;
    private Context context;

    public GateWayListAdapter(List<GateDetail.SubListBean> list) {
        this.list = list;
    }

    @Override
    public GateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_gateway, parent, false);
        return new GateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GateViewHolder holder, final int position) {
        final GateDetail.SubListBean bean = list.get(position);
        holder.binding.setData(bean);
        final String devId = bean.getDevId();
        final String substring = devId.substring(0, 3);
        String type = null;
        switch (substring) {
            case "000":
                type = "智能网关";
                break;
            case "C00":
                type = "智能门磁";
                break;
            case "C01":
                type = "人体红外";
            case "A00":
                String substring1 = devId.substring(0, 4);
                if ("A001".equals(substring1)) {
                    type = "T600网络版智能锁";
                } else if ("A000".equals(substring1)) {
                    type = "T600单机版智能锁";
                }
                break;
            default:
                type = "设备";
                break;
        }
        holder.binding.gatewayItemType.setText(type);
        holder.binding.gatewayItemRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                Intent intent;
                if ("C00".equals(substring) || "C01".equals(substring)) {
                    b.putString("DeviceId", devId);
                    intent = new Intent(context, MagnetometerActivity.class);
                } else {
//                    (String pid, int state, int isOnline, String power, long timeBind, String nickName, int isCall) {
                    LockState lockState = new LockState(bean.getDevId(), 0,
                            bean.getIsOnline(), bean.getPower(), bean.getTimeBind(), bean.getDevName(),
                            0);
                    b.putParcelable("lockState", lockState);
                    intent = new Intent(context, EquipmentActivity.class);
                }
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class GateViewHolder extends RecyclerView.ViewHolder {

        private ItemGatewayBinding binding;

        public GateViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
