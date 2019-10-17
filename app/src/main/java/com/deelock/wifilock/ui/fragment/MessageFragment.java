package com.deelock.wifilock.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.MessageAdapter;
import com.deelock.wifilock.databinding.FragmentMessageBinding;
import com.deelock.wifilock.entity.Message;
import com.deelock.wifilock.entity.PushList;
import com.deelock.wifilock.entity.PushMessage;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.ActDialog;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\19 0019.
 */

public class MessageFragment extends BaseFragment {

    FragmentMessageBinding binding;

    List<Message> data;
    MessageAdapter adapter;
    BroadcastReceiver receiver;
    LocalBroadcastManager broadcastManager;

    private boolean isFront;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        doBusiness();
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcast();
    }

    private void doBusiness() {
        data = new ArrayList<>();
        adapter = new MessageAdapter(data);
        binding.rv.setAdapter(adapter);
        InitRecyclerView.initLinearLayoutVERTICAL(getContext(), binding.rv);
        adapter.setEvent(new MessageAdapter.Event() {
            @Override
            public void delete(String id) {
                Map params = new HashMap<>();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(getActivity()));
                params.put("alertId", id);
                RequestUtils.request(RequestUtils.MESSAGE_DELETE, getActivity(), params).enqueue(
                        new ResponseCallback<BaseResponse>(getActivity()) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                            }
                        }
                );
            }
        });
    }

    private void registerBroadcast() {
        broadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter("com.deelock.wifilock.LOCALBROADCAST");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                SimpleDateFormat sf = new SimpleDateFormat("HH:mm  MM/dd");
                final PushMessage message = intent.getParcelableExtra("push");
                if (!"A00".equals(message.getDeviceId().substring(0, 3))) {
                    return;
                }
                if (message.getCode() < 10001000 || message.getCode() > 10001008) {
                    return;
                }
                StringBuilder msg = new StringBuilder("您的门锁于").append(sf.format(message.getTimePush() * 1000l)).append("发生").append(message.getTitle());
                msg.append("异常，请及时处理！");
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                    AlertDialog alertDialog;
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("报警提醒").setMessage(msg.toString())
//                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//
//                    alertDialog = builder.create();
//                    alertDialog.setCancelable(false);
//                    alertDialog.setCanceledOnTouchOutside(false);
//                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                    alertDialog.show();
//                } else {
                    Intent i = new Intent(context, ActDialog.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("msg", msg.toString());
                    context.startActivity(i);
//                }
            }
        };
        broadcastManager.registerReceiver(receiver, intentFilter);
    }

    private void loadData() {
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(getContext()));
        params.put("count", 100);
        params.put("minTime", -1);
        params.put("maxTime", -1);
        RequestUtils.request(RequestUtils.MESSAGE, getActivity(), params).enqueue(
                new ResponseCallback<BaseResponse>(getActivity()) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        data.clear();
                        data.addAll(new Gson().fromJson(content, PushList.class).getList());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        isFront = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFront = false;
    }
}
