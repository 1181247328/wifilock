package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.view.View;

import com.deelock.wifilock.R;
import com.deelock.wifilock.constants.Constants;
import com.deelock.wifilock.databinding.ActivityGestureBinding;
import com.deelock.wifilock.overwrite.GestureLockViewGroup;
import com.deelock.wifilock.utils.StatusBarUtil;
import com.deelock.wifilock.utils.ToastUtil;

/**
 * Created by binChuan on 2017\9\28 0028.
 */

public class GestureActivity extends BaseActivity {

    ActivityGestureBinding binding;
    private boolean isSetGesture;
    private boolean isFirst;
    private int tryTimes = 5;
    private boolean needEvaluate;
    private boolean isDelete;

    @Override
    protected void bindActivity() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gesture);
    }

    @Override
    protected void doBusiness() {
        StatusBarUtil.StatusBarLightMode(this);
        isSetGesture = getIntent().getBooleanExtra("isSetGesture", false);
        isDelete = getIntent().getBooleanExtra("isDelete", false);
        isFirst = getIntent().getBooleanExtra("isFirst", false);
        needEvaluate = getIntent().getBooleanExtra("needEvaluate", false);
        binding.gl.setFlag(isSetGesture);
        if (isSetGesture) {
            binding.titleTv.setText("请设置手势密码");
        } else {
            binding.titleTv.setText("请验证手势密码");
        }
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

        binding.gl.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {

            }

            @Override
            public void onGestureEvent(boolean matched) {
                if (isSetGesture) {
                    return;
                }

                if (!matched) {
                    tryTimes--;
                    ToastUtil.toastLong(getApplicationContext(), "您还有" + tryTimes + "次尝试机会！");
                    if (tryTimes < 1) {
                        ToastUtil.toastLong(getApplicationContext(), "手势密码验证失败，请验证登录密码");
                        finish();
                        startActivity(new Intent(GestureActivity.this, VerifyPasswordActivity.class));
                    }
                } else {
                    if (isDelete) {
                        Intent intent = new Intent();
                        intent.putExtra("isDelete", true);
                        setResult(0, intent);
                    }
                    Constants.isVerifiedGesture = true;
                    ToastUtil.toastLong(getApplicationContext(), "手势密码验证成功");
//                    if (needEvaluate){
//                        startActivity(new Intent(GestureActivity.this, EvaluateActivity.class));
//                    } else {
//                        finish();
//                    }
                    finish();
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {

            }

            @Override
            public void setGestureSucceed() {
                if (!isFirst) {
                    finish();
                    return;
                }
                Constants.isVerifiedGesture = true;
                Intent intent = new Intent(GestureActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}
