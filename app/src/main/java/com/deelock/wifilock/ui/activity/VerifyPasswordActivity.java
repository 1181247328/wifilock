package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityVerifyPasswordBinding;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.StatusBarUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\10\13 0013.
 */

public class VerifyPasswordActivity extends BaseActivity {

    ActivityVerifyPasswordBinding binding;
    private boolean isVerify;
    private boolean isDelete;
    private boolean isRestore;  //是否恢复出厂设置

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_password);
    }

    @Override
    protected void doBusiness() {
        StatusBarUtil.StatusBarLightMode(this);
        setWifiPasswordInputFilter(binding.passwordEt);
        isDelete = getIntent().getBooleanExtra("isDelete", false);
        isVerify = getIntent().getBooleanExtra("isVerify", false);
        isRestore = getIntent().getBooleanExtra("isRestore", false);
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

        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPassword();
            }
        });

    }

    private void verifyPassword() {
        if (!isNetworkAvailable()) {
            return;
        }

        String password = binding.passwordEt.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入登录密码", Toast.LENGTH_SHORT).show();
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("pid", SPUtil.getUid(this));
        params.put("pwd", password);
        RequestUtils.request(RequestUtils.VERIFY_PASSWORD, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        super.onSuccess(code, content);
                        if (isRestore) {
                            setResult(RESULT_OK);
                            finish();
                            return;
                        }
                        if (Constants.isVerifiedGestureFailed) {
                            Constants.isVerifiedGestureFailed = false;
                            finish();
                            return;
                        }
                        if (isVerify) {
                            if (isDelete) {
                                Intent intent = new Intent();
                                intent.putExtra("isDelete", true);
                                setResult(0, intent);
                            }
                            Constants.isVerifiedPassword = true;
                            finish();
                            return;
                        }
                        Intent intent = new Intent(VerifyPasswordActivity.this, GestureActivity.class);
                        intent.putExtra("isSetGesture", true);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    protected void onFailure(int code, String message) {
                        super.onFailure(code, message);
                        Toast.makeText(VerifyPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
