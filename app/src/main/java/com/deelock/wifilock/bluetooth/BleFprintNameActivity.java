package com.deelock.wifilock.bluetooth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBleFprintNameBinding;
import com.deelock.wifilock.entity.FPrintList;
import com.deelock.wifilock.entity.UserFPrint;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.GsonUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleFprintNameActivity extends BaseActivity<ActivityBleFprintNameBinding> {

    private String authId;
    private String deviceId;
    private String mac;
    private List<UserFPrint> list;
    private String fprintId;

    @Override
    protected int initLayout() {
        return R.layout.activity_ble_fprint_name;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        binding.setClick(this);
        StatusBarUtil.StatusBarLightMode(this);
        mac = getIntent().getStringExtra("mac");
        authId = getIntent().getStringExtra("authId");
        deviceId = getIntent().getStringExtra("deviceId");
//        fprintId = getIntent().getStringExtra("fprintId");
        getAllFprint();
    }

    public void rightThumb() {
        binding.bleFprintName.setText("右手拇指");
    }

    public void rightIndex() {
        binding.bleFprintName.setText("右手食指");
    }

    public void rightMiddle() {
        binding.bleFprintName.setText("右手中指");
    }

    public void leftThumb() {
        binding.bleFprintName.setText("左手拇指");
    }

    public void leftIndex() {
        binding.bleFprintName.setText("左手食指");
    }

    public void leftMiddle() {
        binding.bleFprintName.setText("左手中指");
    }

    public void save() {
        if (list == null) {
            Toast.makeText(this, "请等待服务配置完成", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = 0;
        String name = binding.bleFprintName.getText().toString().trim();
        for (UserFPrint u : list) {
            count++;
            if ("name".equals(u.getOpenName())) {
                break;
            }
        }
        if (count != list.size()) {
            Toast.makeText(this, "存在重复昵称，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("timestamp", String.valueOf(TimeUtil.getTime()));
        map.put("uid", SPUtil.getUid(this));
        map.put("sdlId", deviceId);
        map.put("type", "0");
        map.put("authId", authId);
        map.put("openName", name);

        RequestUtils.request(RequestUtils.BLE_ADD_FPRINT, this, map)
                .enqueue(new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        ToastUtil.toastShort(BleFprintNameActivity.this, "指纹添加成功");
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        ToastUtil.toastShort(BleFprintNameActivity.this, message);
                        finish();
                    }
                });
    }

    private void getAllFprint() {
        Map params = new HashMap<>();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", deviceId);
        RequestUtils.request(RequestUtils.ALL_FPRINT, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        FPrintList fPrintList = GsonUtil.json2Bean(content, FPrintList.class);
                        list = fPrintList.getList();
                    }
                }
        );
    }
}
