package com.deelock.wifilock.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.deelock.wifilock.R;
import com.deelock.wifilock.common.BaseActivity;
import com.deelock.wifilock.databinding.ActivityBindGateWayBinding;
import com.deelock.wifilock.entity.GateBind;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BindGateWayActivity extends BaseActivity<ActivityBindGateWayBinding> {

    private ProgressDialog mProgressDialog;
    private CompositeDisposable comDisposable;
    Disposable disposable;
    private String wifi;
    private String bssid;
    private String password;
    double latitude = 0;
    double longitude = 0;

    @Override
    protected int initLayout() {
        return R.layout.activity_bind_gate_way;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        wifi = getIntent().getStringExtra("wifi");
        bssid = getIntent().getStringExtra("bssid");
        password = getIntent().getStringExtra("password");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        comDisposable = new CompositeDisposable();
        binding.setClick(this);
    }

    public void onBackClicked() {
        finish();
    }

    public void onNextClicked() {
        new EsptouchAsyncTask3().execute(wifi, bssid, password, "1");
        final int[] a = {0};
        Observer<Long> observer = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                HashMap<String, String> params = new HashMap<>();
                params.put("timestamp", String.valueOf(TimeUtil.getTime()));
                params.put("uid", SPUtil.getUid(BindGateWayActivity.this));
                params.put("ssid", wifi);
                params.put("longitude", String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));
                params.put("devType", "000");
                RequestUtils.request(RequestUtils.GATEWAY_BIND, BindGateWayActivity.this, params)
                        .enqueue(new ResponseCallback<BaseResponse>(BindGateWayActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                super.onSuccess(code, content);
                                if (code == 1) {
                                    disposable.dispose();
                                    if (mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }
                                    comDisposable.clear();
                                    Toast.makeText(BindGateWayActivity.this, "网关已绑定", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BindGateWayActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else if (code == 2) {
                                    if (a[0] == 60) {
                                        disposable.dispose();
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                        }
                                        comDisposable.clear();
                                        Toast.makeText(BindGateWayActivity.this, "绑定超时", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            protected void onFailure(int code, String message) {
                                super.onFailure(code, message);
                                Toast.makeText(BindGateWayActivity.this, message, Toast.LENGTH_SHORT).show();
                                disposable.dispose();
                                comDisposable.clear();
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {
                disposable.dispose();
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onComplete() {

            }
        };
        Observable.interval(0, 5, TimeUnit.SECONDS).subscribe(observer);
        comDisposable.add(disposable);
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private IEsptouchTask mEsptouchTask;
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(BindGateWayActivity.this);
            mProgressDialog.setMessage("正在连接网关...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                        if (disposable != null) {
                            comDisposable.clear();
                        }
                    }
                }
            });
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("TAG", "progress dialog is canceled");
                        }
                        if (disposable != null) {
                            comDisposable.clear();
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = 1;
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, BindGateWayActivity.this);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            if (!firstResult.isCancelled()) {
                int count = 0;
                final int maxDisplayCount = 5;
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage("配置网关成功，正在连接服务器...");
                } else {
                    mProgressDialog.dismiss();
                    ToastUtil.toastShort(getApplicationContext(), "配置网关失败");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            comDisposable.clear();
        }
    }
}
