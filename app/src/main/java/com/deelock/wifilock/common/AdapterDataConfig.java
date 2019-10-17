package com.deelock.wifilock.common;

import android.databinding.BindingAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.deelock.wifilock.adapter.GateWayListAdapter;
import com.deelock.wifilock.adapter.SelectGateWayAdapter;
import com.deelock.wifilock.entity.GateDetail;
import com.deelock.wifilock.entity.GateWay;

import java.util.List;

public class AdapterDataConfig {

    @BindingAdapter("gateway")
    public static void setGateWayList(RecyclerView view, List<GateDetail.SubListBean> list) {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        view.setAdapter(new GateWayListAdapter(list));
    }

    @BindingAdapter("selectGate")
    public static void setSelectGateWayList(RecyclerView view, List<GateWay> list) {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        view.setAdapter(new SelectGateWayAdapter(list));
    }

}
