package com.deelock.wifilock.ui.activity;

import android.databinding.DataBindingUtil;
import android.text.TextUtils;

import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.EvaluateAdapter;
import com.deelock.wifilock.databinding.ActivityEvaluateBinding;
import com.deelock.wifilock.entity.Evaluate;
import com.deelock.wifilock.entity.EvaluateItem;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by forgive for on 2018\1\15 0015.
 */

public class EvaluateActivity extends BaseActivity {

    ActivityEvaluateBinding binding;
    EvaluateAdapter adapter;
    private List<EvaluateItem> items;
    private String sdlId;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_evaluate);
    }

    @Override
    protected void doBusiness() {
        items = new ArrayList<>();
        adapter = new EvaluateAdapter(items);
        binding.rv.setAdapter(adapter);
        sdlId = getIntent().getStringExtra("sdlId");
    }

    @Override
    protected void requestData() {
        if (TextUtils.isEmpty(sdlId)){
            return;
        }
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        RequestUtils.request(RequestUtils.EVALUATE_ITEMS, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        items.addAll(new Gson().fromJson(content, Evaluate.class).getItemList());
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    @Override
    protected void setEvent() {

    }
}
