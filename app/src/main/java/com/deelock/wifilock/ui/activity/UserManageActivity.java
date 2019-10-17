package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.deelock.wifilock.R;
import com.deelock.wifilock.adapter.UserManageAdapter;
import com.deelock.wifilock.databinding.ActivityUserManageBinding;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.entity.UserList;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.InitRecyclerView;
import com.deelock.wifilock.utils.SPUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\22 0022.
 */

public class UserManageActivity extends BaseActivity {

    ActivityUserManageBinding binding;
    private String sdlId;
    private ArrayList<User> data = new ArrayList<>();
    private UserManageAdapter adapter;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_manage);
    }

    @Override
    protected void doBusiness() {
        sdlId = getIntent().getStringExtra("sdlId");
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {
        binding.backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.addIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data == null) {
                    Toast.makeText(UserManageActivity.this, "请稍后再试", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(UserManageActivity.this, AddUserActivity.class);
                intent.putExtra("sdlId", sdlId);
                intent.putParcelableArrayListExtra("userList", (ArrayList<? extends Parcelable>) data);
                startActivity(intent);
            }
        });
    }

    private void getData() {
        if (!isNetworkAvailable()) {
            return;
        }

        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("authId", sdlId);
        String url;
        if ("A003".equals(sdlId.substring(0, 4))) {
            url = RequestUtils.BLE_USER_LIST;
        } else {
            url = RequestUtils.USER_LIST;
        }
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        data = new ArrayList<>();
                        data.addAll(new Gson().fromJson(content, UserList.class).getList());
                        Collections.sort(data);
//                        if (sdlId.substring(0, 4).equals("A002")){
//                            data.get(0).setName("租客");
//                            data.get(1).setName("房东");
//                        }
                        adapter = new UserManageAdapter(UserManageActivity.this, data, sdlId);
                        binding.rv.setAdapter(adapter);
                        InitRecyclerView.initLinearLayoutVERTICAL(UserManageActivity.this, binding.rv);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}
